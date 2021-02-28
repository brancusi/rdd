(ns rdd.components.bom-row.view
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [re-frame.core :as rf :refer [subscribe]]
   [re-com.core   :refer [input-text button]]))

(defn bom-row
  [_ node _]
  (let [text-val (reagent/atom (:name node))]

    (reagent/create-class
     {:component-did-update
      (fn [this [_ old-id _ _]]
        (let [[new-id node _] (rest (reagent/argv this))
              new-name (:name node)]
          (if (not (= new-id old-id))
            (reset! text-val new-name)
            nil)))

      :reagent-render
      (fn [_ _ update-node]
        [:div
         [input-text
          :model @text-val
          :change-on-blur? false
          :on-change #(reset! text-val %)]
         [button
          :label "Save"
          :on-click (fn []
                      (update-node {:name @text-val}))]])})))


