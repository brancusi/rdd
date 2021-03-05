(ns rdd.db
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as timbre
             :refer-macros [log info spy]]
            [clojure.spec.alpha :as s]))


(def default-db
  {:active-node "burrito"

   :editing/active-master-node "sauce-1"

   :nodes {"salt"         {:name "Salt"
                           :id "salt"
                           :yield 1
                           :yield-uom :gram}

           "pepper"       {:name "Pepper"
                           :id "pepper"
                           :yield 1
                           :yield-uom :gram}

           "pepper2"       {:name "Pepper 2"
                            :id "pepper2"
                            :yield 1
                            :yield-uom :gram}


           "pepper3"       {:name "Pepper 3"
                            :id "pepper3"
                            :yield 1
                            :yield-uom :gram}


           "pepper4"       {:name "Pepper 4"
                            :id "pepper4"
                            :yield 1
                            :yield-uom :gram}

           "pepper5"       {:name "Pepper 5"
                            :id "pepper5"
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
                                   :order 1}

           "sauce-1-pepper"       {:child-node "pepper"
                                   :edge-id "sauce-1-pepper"
                                   :qty 10
                                   :uom :gram
                                   :order 2}

           "burrito-sauce-1"  {:child-node "sauce-1"
                               :edge-id "burrito-sauce-1"
                               :qty 100
                               :uom :gram
                               :order 1}}

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
                       :additional-cost 0}]

           "pepper4"  [{:cost 2
                        :qty 1
                        :uom :pound
                        :date 1
                        :additional-cost 0}]}})

(defn seed-db
  []
  (rf/dispatch [:reset-db-with default-db]));