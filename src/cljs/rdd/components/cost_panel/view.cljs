(ns rdd.components.cost-panel.view
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]

   [rdd.components.add-node-row.subs]
   [rdd.components.node-editor.subs]
   [rdd.components.yield-editor.view :refer [yield-editor]]
   [rdd.components.node-editor.fx]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [rdd.components.rows.node-row.view :refer [node-row]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))

(defn cost-panel
  [{:keys [recipe-cost] :as tree}]
  [h-box
   :class "text-white bg-gray-500 p-4 my-4"

   :children [[:p (str "Total cost of this recipe: " (gstring/format "%.2f" recipe-cost))]]])