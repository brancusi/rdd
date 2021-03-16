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
   [rdd.components.inputs.uom-drop-down.view :refer [uom-drop-down]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))


(defn quantity-editor
  [nt]
  (let [{:keys [edge-id qty uom]} nt


        update-edge (fn
                      [key [val _]]
                      (rf/dispatch [:update-edge edge-id {key val}]))

        create-new-uom (fn [label]
                         (rf/dispatch [:create-and-link-uom-edge edge-id :uom label]))]
    [v-box
     :children [[h-box
                 :src (at)
                 :children [[:span

                             [input-text
                              :src (at)
                              :model (str qty)
                              :width "100px"
                              :on-change #(update-edge :qty [%])]]

                            [uom-drop-down {:model uom
                                            :create-fn create-new-uom
                                            :update-fn (partial update-edge :uom)}]]]]]))
