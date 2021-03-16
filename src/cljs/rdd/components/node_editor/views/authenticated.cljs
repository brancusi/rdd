(ns rdd.components.node-editor.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [cljs.pprint :refer [pprint]]
   [goog.string :as gstring]
   [rdd.components.add-node-row.subs]
   [rdd.components.node-editor.subs]
   [rdd.components.yield-editor.view :refer [yield-editor]]
   [rdd.components.cost-panel.view :refer [cost-panel]]

   [rdd.components.node-editor.fx]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [rdd.components.rows.node-row.view :refer [node-row]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))

(defn node-editor
  [{:keys [id name children] :as tree}]

  [v-box
   :children [[:h1 {:class "my-8 text-4xl"} name]

              [h-box
               :align :center
               :children [[label
                           :class "mr-4"
                           :label "This recipe makes"]
                          (yield-editor tree)]]

              (cost-panel tree)

              (for [{:keys [edge-id] :as node} children]
                ^{:key edge-id} [node-row
                                 {:tree node
                                  :parent-node-id id}])

              [edn->hiccup tree]]])
