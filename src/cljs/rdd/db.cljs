(ns rdd.db
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as timbre
             :refer-macros [log info spy]]
            [clojure.spec.alpha :as s]))

(def default-db
  {:active-node "burrito"

   :nodes {"salt"         {:name "Salt"
                           :id "salt"}

           "pepper"       {:name "Pepper"
                           :id "pepper"}

           "paprika"      {:name "Paprika"
                           :id "paprika"}

           "sauce-1"      {:name "Sauce 1"
                           :id "sauce-1"
                           :child-edges ["sauce-1-salt" "sauce-1-pepper"]}

           "sauce-2"      {:name "Sauce 2"
                           :id "sauce-2"
                           :child-edges ["sauce-2-pepper" "sauce-2-paprika"]}

           "sauce-blend"  {:name "Sauce Blend"
                           :id "sauce-blend"
                           :child-edges ["sauce-blend-sauce-1" "sauce-blend-sauce-2"]}

           "burrito"      {:name "Burrito"
                           :id "burrito"
                           :child-edges ["burrito-sauce-blend"]}}

   :edges {"sauce-1-salt"         {:child-node "salt"
                                   :qty 10
                                   :uom :gram
                                   :order 1}

           "sauce-1-pepper"       {:child-node "pepper"
                                   :qty 10
                                   :uom :gram
                                   :order 2}

           "sauce-2-pepper"       {:child-node "pepper"
                                   :qty 10
                                   :uom :gram
                                   :order 1}

           "sauce-2-paprika"      {:child-node "paprika"
                                   :qty 10
                                   :uom :gram
                                   :order 2}

           "sauce-blend-sauce-1"  {:child-node "sauce-1"
                                   :qty 10
                                   :uom :gram
                                   :order 1}

           "sauce-blend-sauce-2"  {:child-node "sauce-2"
                                   :qty 10
                                   :uom :gram
                                   :order 1}

           "burrito-sauce-blend"  {:child-node "sauce-blend"
                                   :qty 10
                                   :uom :gram
                                   :order 1}}

   :conversions {"salt" {:cup    {:gram 10}
                         :case   {:pack 25}
                         :pack   {:pound 1}}}

   :costs {"salt"    [{:cost 10
                       :qty 10
                       :uom :pound
                       :date 10000000000
                       :additional-cost 10}]

           "pepper"  [{:cost 30
                       :qty 1
                       :uom :case
                       :date 10000000000
                       :additional-cost 10}]

           "paprika" [{:cost 40
                       :qty 2
                       :uom :case
                       :date 10000000000
                       :additional-cost 10}]}})

(def hey {:cup    {:gram 10}
          :case   {:pack 25}
          :pack   {:pound 1}})


(defn conversion-index
  [conversion]
  (reduce
   (fn
     [[acc] [from-uom val]]
     (for [[to-uom qty] (seq val)]
       (assoc-in acc [to-uom from-uom] (/ 1 qty))))
   (list conversion)
   (map (fn
          [[key val]]
          [key val]) conversion)))

(comment
  (conversion-index (get-in default-db [:conversions "salt"]))

  ;; 
  )


(info "Acc" {:a 10})

  ;; 


(assoc-in {} [:salt :pack] 10)

(seq {:a 10})

(for [yo (keys {:a 10 :b 20})]
  (info yo))

(defn seed-db
  []
  (rf/dispatch [:reset-db-with default-db]));