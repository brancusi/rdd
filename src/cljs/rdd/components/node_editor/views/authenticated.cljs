(ns rdd.components.node-editor.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at]]))

(defn style []
  {:color "red"
   :font-size "2rem"})

(defn row-style []
  {:padding-left "2rem"})

(defn node-row
  [nt]
  (let [{:keys [id
                yield
                edge-id
                order
                name
                qty
                children
                recipe-cost
                cost-per-uom
                yield-uom
                uom]} nt]
    (info nt)
    [:div [:span
           {:class (<class style)}
           (str name " - " " Recipe Cost: " (gstring/format "%.2f" (str recipe-cost)) " Cost per UOM: " (gstring/format "%.2f" (str cost-per-uom)) " yield " yield)]
     [:span [input-text
             :src (at)
             :model (str qty)
             :on-change (fn [hey]
                          (dispatch [:update-edge edge-id {:qty hey}]))]]
     [single-dropdown
      :src (at)
      :model uom
      :choices [{:id :gram :label "Gram"}
                {:id :pound :label "Pound"}
                {:id :kilogram :label "Kilogram"}]
      :on-change (fn [choice]
                   (info choice)
                   (dispatch [:update-edge edge-id {:uom choice}]))]

     [:span [input-text
             :src (at)
             :model (str yield)
             :on-change (fn [hey]
                          (dispatch [:update-node id {:yield hey}]))]]
     [single-dropdown
      :src (at)
      :model yield-uom
      :choices [{:id :gram :label "Gram"}
                {:id :pound :label "Pound"}
                {:id :kilogram :label "Kilogram"}]
      :on-change (fn [choice]
                   (info choice)
                   (dispatch [:update-node id {:yield-uom choice}]))]

     (if-let [children children]
       [:div
        {:class (<class row-style)}
        (for [child (sort-by :order children)]
          (let [id (:id child)
                order (:order child)
                key (str id "-" order)]
            ^{:key key} [node-row child]))])]))

(comment
  (rf/dispatch [:update-edge "sauce-1-salt" {:order 1}])
  (rf/dispatch [:set-active-node "sauce-1"])
  (rf/dispatch [:set-active-node "sauce-2"])
  (rf/dispatch [:set-active-node "burrito"])
  (rf/dispatch [:update-edge "sauce-1-salt" {:qty 20}])

  ;; 
  )

(defn node-editor
  [nt]
  [:div
   [input-text
    :model (:name nt)
    :on-change (fn [x] (info x))
    :src (at)]
   [node-row nt]])

