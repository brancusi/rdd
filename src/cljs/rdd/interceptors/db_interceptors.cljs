(ns rdd.interceptors.db-interceptors
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [rdd.db :refer [default-db]]
   [rdd.utils.db-utils :as db-utils]
   [clojure.data :as data]))

(def re-link-edges
  (rf/->interceptor
   :id :re-link-edges
   :after (fn [context]
            (js-debugger))))


(defn doit
  [db edge-id]
  (let [edge-siblings (db-utils/edge-siblings db edge-id)]))

;; (doit default-db "sauce-1-salt")


(defn sort-indexes
  [col id]
  (->> (sort (fn [x y]
               (let [x-id (:id x)
                     x-index (:index x)
                     y-index (:index y)]
                 (if (= x-index y-index)
                   (= id x-id)
                   (< x-index y-index))))
             col)
       vec))

(def re-index-edges
  (rf/->interceptor
   :id :re-index-edges
   :before (fn
             [context]
             (let [db (rf/get-coeffect context :db)]


               (rf/assoc-coeffect context :db db)))
   :after (fn
            [context]


            (let [db (rf/get-effect context :db)
                  [_ edge-id _ _] (do
                                    (info "here" (rf/get-coeffect context :event))
                                    (rf/get-coeffect context :event))
                  siblings (do
                             (info edge-id)
                             (info db)
                             (info (db-utils/edge-siblings db edge-id))
                             (db-utils/edge-siblings db edge-id))
                  sorted-siblings (sort-indexes siblings edge-id)


                  re-indexed-edges (map-indexed (fn [idx edge]
                                                  (assoc edge :index (inc idx) :another "HI"))
                                                sorted-siblings)

                  updated-db (reduce
                              (fn [acc {:keys [edge-id] :as edge}]
                                (assoc-in acc [:edges edge-id] edge))
                              db
                              re-indexed-edges)]

              (rf/assoc-effect context :db updated-db)))))


(sort-indexes "salt" [{:index 1 :id "pepper"}
                      {:index 10 :id "other"}
                      {:index 2 :id "cayanne"}
                      {:index 5 :id "thyme"}
                      {:index 1 :id "salt"}
                      {:index 4 :id "cumin"}
                      {:index 9 :id "wheat"}])
