(ns rdd.db
  (:require [re-frame.core :as rf]))

(def default-db
  {:active-node "burrito"
   :nodes
   {"salt" {:name "Salt"
            :child-edges []}

    "pepper" {:name "Pepper"
              :child-edges []}

    "paprika" {:name "Paprika"
               :child-edges []}

    "sauce-1" {:name "Sauce 1"
               :child-edges ["sauce-1-salt" "sauce-1-pepper"]}

    "sauce-2" {:name "Sauce 2"
               :child-edges ["sauce-2-pepper" "sauce-2-paprika"]}

    "sauce-blend" {:name "Sauce Blend"
                   :child-edges ["sauce-blend-sauce-1" "sauce-blend-sauce-2"]}

    "burrito" {:name "Burrito"
               :child-edges ["burrito-sauce-blend"]}}

   :edges {"sauce-1-salt" {:child-node "salt" :qty 10}
           "sauce-1-pepper" {:child-node "pepper" :qty 10}

           "sauce-2-pepper" {:child-node "pepper" :qty 10}
           "sauce-2-paprika" {:child-node "paprika" :qty 10}

           "sauce-blend-sauce-1" {:child-node "sauce-1" :qty 10}

           "sauce-blend-sauce-2" {:child-node "sauce-2" :qty 10}

           "burrito-sauce-blend" {:child-node "sauce-blend" :qty 10}}})


(defn seed-db
  []
  (rf/dispatch [:reset-db-with default-db]));