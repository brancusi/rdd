(ns rdd.db
  (:require [re-frame.core :as rf]
            [cljs.spec.gen.alpha :as gen]
            [taoensso.timbre :as timbre
             :refer-macros [log info spy]]
            [clojure.spec.alpha :as spec]))



(def default-db
  {:selected-node "burrito"

   :editing {:master-node/id "burrito"
             :state [:editing :adding-node]}

   :states {:node-editor {:state :adding-node
                          :node/id "sauce-1"
                          :previous-index 1}}

   :nodes {"salt"         {:name "Salt"
                           :id "salt"
                           :yield 1
                           :yield-uom :gram}

           "pepper"       {:name "Pepper"
                           :id "pepper"
                           :yield 1
                           :yield-uom :gram}

           "sauce-1"      {:name "Sauce 1"
                           :id "sauce-1"
                           :yield 1
                           :yield-uom :kilogram
                           :child-edges ["sauce-1-salt" "sauce-1-pepper"]}

           "burrito"      {:name "Burrito"
                           :id "burrito"
                           :yield 1
                           :yield-uom "burrito"
                           :child-edges ["burrito-sauce-1"]}}

   :edges {"sauce-1-salt"         {:child-node "salt"
                                   :edge-id "sauce-1-salt"
                                   :qty 10
                                   :uom :gram
                                   :index 1}

           "sauce-1-pepper"       {:child-node "pepper"
                                   :edge-id "sauce-1-pepper"
                                   :qty 10
                                   :uom :gram
                                   :index 2}

           "burrito-sauce-1"  {:child-node "sauce-1"
                               :edge-id "burrito-sauce-1"
                               :qty 100
                               :uom :gram
                               :index 1}}

   :conversions {"salt" {:cup    {:gram 10}
                         :case   {:pack 25}
                         :pinch  {:gram 0.2}
                         :pack   {:pound 1}}
                 "sauce-1" {"burrito" {:gram 100}}}

   :costs {"salt"    [{:cost 2
                       :qty 1
                       :uom :pound
                       :date 1
                       :additional-cost 0}]

           "pepper"  [{:cost 2
                       :qty 1
                       :uom :pound
                       :date 1
                       :additional-cost 0}]}})

(defn seed-db
  []
  (rf/dispatch [:reset-db-with default-db]));

