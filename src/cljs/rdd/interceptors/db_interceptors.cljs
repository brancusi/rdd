(ns rdd.interceptors.db-interceptors
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]
   [nano-id.core :refer [nano-id]]
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

(defn generate-uuid
  "Interceptor to generate a UUID. Add a key to assoc in the effects map to be used by other interceptors and fx"
  [key]
  (rf/->interceptor
   :id :generate-uuid
   :before (fn
             [context]
             (let [current-val (rf/get-coeffect context key)]
               (when (nil? current-val)
                 (rf/assoc-coeffect context key (nano-id)))))))

(def re-index-edges
  "Interceptor to re-index the edges in order based on adding of new edges.
   This interceptor works in tandem with the (generate-uuid :edge-id).
   This puts a key in the coeffects map that will be used to insert the new edge"
  (rf/->interceptor
   :id :re-index-edges
   :after (fn
            [context]
            (let [db (rf/get-effect context :db)
                  edge-id (or
                           (rf/get-coeffect context :edge-id)
                           (get (rf/get-coeffect context :original-event) 1))
                  siblings (db-utils/edge-siblings db edge-id)
                  sorted-siblings (sort-indexes siblings edge-id)
                  re-indexed-edges (map-indexed (fn [idx edge]
                                                  (assoc edge :index (inc idx)))
                                                sorted-siblings)
                  updated-db (reduce
                              (fn [acc {:keys [edge-id] :as edge}]
                                (assoc-in acc [:edges edge-id] edge))
                              db
                              re-indexed-edges)]
              (rf/assoc-effect context :db updated-db)))))