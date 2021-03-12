(ns rdd.components.quantity-editor.view
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]
   [rdd.components.add-node-row.subs]
   [rdd.components.node-editor.subs]
   [rdd.components.node-editor.fx]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))


(defn quantity-editor
  [nt]
  (let [{:keys [edge-id qty uom]} nt]
    [v-box
     :children [[h-box
                 :src (at)
                 :children [[:span

                             [input-text
                              :src (at)
                              :model (str qty)
                              :width "100px"
                              :on-change (fn [val]
                                           (dispatch [:update-edge edge-id {:qty val}]))]]
                            [single-dropdown
                             :src (at)
                             :model uom
                             :class "ml-2"
                             :choices [{:id :gram :label "Gram"}
                                       {:id :pound :label "Pound"}
                                       {:id :kilogram :label "Kilogram"}]
                             :on-change (fn [choice]
                                          (dispatch [:update-edge edge-id {:uom choice}]))]]]]]))
