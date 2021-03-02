(ns rdd.db
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as timbre
             :refer-macros [log info spy]]
            [clojure.spec.alpha :as s]))

(def default-db
  {:active-node "burrito"

   :nodes {"salt"         {:name "Salt"
                           :id "salt"
                           :yield 1
                           :yield-uom :gram}

           "pepper"       {:name "Pepper"
                           :id "pepper"
                           :yield 1
                           :yield-uom :gram}

           "paprika"      {:name "Paprika"
                           :id "paprika"
                           :yield 1
                           :yield-uom :gram}

           "sauce-1"      {:name "Sauce 1"
                           :id "sauce-1"
                           :yield 1
                           :yield-uom :gram
                           :child-edges ["sauce-1-salt" "sauce-1-pepper"]}

           "sauce-2"      {:name "Sauce 2"
                           :id "sauce-2"
                           :yield 2
                           :yield-uom :gram
                           :child-edges ["sauce-2-pepper" "sauce-2-paprika"]}

           "sauce-blend"  {:name "Sauce Blend"
                           :id "sauce-blend"
                           :yield 2
                           :yield-uom :gram
                           :child-edges ["sauce-blend-sauce-1" "sauce-blend-sauce-2"]}

           "burrito"      {:name "Burrito"
                           :id "burrito"
                           :yield 1
                           :yield-uom "burrito"
                           :child-edges ["burrito-sauce-blend"]}}

   :edges {"sauce-1-salt"         {:child-node "salt"
                                   :qty 1
                                   :uom :pound
                                   :order 1}

           "sauce-1-pepper"       {:child-node "pepper"
                                   :qty 1
                                   :uom :pound
                                   :order 2}

           "sauce-2-pepper"       {:child-node "pepper"
                                   :qty 1
                                   :uom :pound
                                   :order 1}

           "sauce-2-paprika"      {:child-node "paprika"
                                   :qty 1
                                   :uom :pound
                                   :order 2}

           "sauce-blend-sauce-1"  {:child-node "sauce-1"
                                   :qty 1
                                   :uom :pound
                                   :order 1}

           "sauce-blend-sauce-2"  {:child-node "sauce-2"
                                   :qty 1
                                   :uom :pound
                                   :order 1}

           "burrito-sauce-blend"  {:child-node "sauce-blend"
                                   :qty 1
                                   :uom :pound
                                   :order 1}}


   :conversions {"salt" {:cup    {:gram 10}
                         :case   {:pack 25}
                         :pinch  {:gram 0.2}
                         :pack   {:pound 1}}}

   :costs {"salt"    [{:cost 1
                       :qty 1
                       :uom :pound
                       :date 1000
                       :additional-cost 0}]

           "pepper"  [{:cost 1
                       :qty 1
                       :uom :pound
                       :date 1000
                       :additional-cost 0}]

           "paprika" [{:cost 1
                       :qty 1
                       :uom :pound
                       :date 1000
                       :additional-cost 0}]}})

(def mini-db
  {:active-node "burrito"

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
                           :yield 650
                           :yield-uom :gram
                           :child-edges ["sauce-1-salt" "sauce-1-pepper"]}

           "burrito"      {:name "Burrito"
                           :id "burrito"
                           :yield 10
                           :yield-uom "burrito"
                           :child-edges ["burrito-sauce-1"]}}

   :edges {"sauce-1-salt"         {:child-node "salt"
                                   :edge-id "sauce-1-salt"
                                   :qty 1
                                   :uom :pound
                                   :order 1}

           "sauce-1-pepper"       {:child-node "pepper"
                                   :edge-id "sauce-1-pepper"
                                   :qty 1
                                   :uom :pound
                                   :order 2}

           "burrito-sauce-1"  {:child-node "sauce-1"
                               :edge-id "burrito-sauce-1"
                               :qty 50
                               :uom :gram
                               :order 1}}

   :conversions {"salt" {:cup    {:gram 10}
                         :case   {:pack 25}
                         :pinch  {:gram 0.2}
                         :pack   {:pound 1}}}

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