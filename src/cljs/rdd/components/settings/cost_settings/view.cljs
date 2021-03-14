(ns rdd.components.settings.cost-settings.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.settings.cost-settings.cost-row.view :refer [cost-row]]
   [cljs.pprint :refer [pprint]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))


(defn cost-settings
  [{:keys [id] :as node} {:keys [edge-id] :as edge}]
  (let [node-costs @(rf/subscribe [:node-costs id])
        create-new-cost (fn []
                          (rf/dispatch [:create-relate-cost id {:cost 1
                                                                :qty 1
                                                                :uom :pound
                                                                :additional-cost 0}]))]
    [v-box
     :children [[v-box :children (for [cost node-costs]
                                   ^{:key (:id cost)} [cost-row cost])]

                [box
                 :class "mt-4"
                 :child [button
                         :label "Create new cost +"
                         :on-click create-new-cost]]]]))