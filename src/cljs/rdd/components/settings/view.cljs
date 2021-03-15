(ns rdd.components.settings.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.yield-editor.view :refer [yield-editor]]
   [rdd.components.settings.conversion-settings.conversion-editor :refer [conversion-editor]]

   [cljs.pprint :refer [pprint]]
   [rdd.components.settings.cost-settings.view :refer [cost-settings]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))


(defn settings-panel
  [{:keys [panel]} update-state node-id edge-id]
  (let [node @(rf/subscribe [:node node-id])
        edge @(rf/subscribe [:edge edge-id])
        tree @(rf/subscribe [:node->tree node-id])
        has-children? (some? (seq (:child-edges node)))]

    [v-box
     :class "bg-gray-200 p-4"
     :children [[h-box
                 :class "mb-4"
                 :children [[button
                             :label "Costs"
                             :on-click #(update-state {:panel :costs})]

                            [button
                             :label "Yield"
                             :on-click #(update-state {:panel :yield})]

                            [button
                             :label "Conversions"
                             :on-click #(update-state {:panel :conversions})]]]

                (case panel
                  :costs [cost-settings node edge]
                  :yield [yield-editor tree]
                  :conversions [conversion-editor tree]
                  :default (if has-children?
                             [yield-editor tree]
                             [cost-settings node edge]))]]))