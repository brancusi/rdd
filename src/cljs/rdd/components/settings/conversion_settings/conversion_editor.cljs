(ns rdd.components.settings.conversion-settings.conversion-editor
  (:require
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [clojure.string :as string]
   [rdd.utils.conversions :as conversions :refer [uoms->grouped-by-type]]
   [rdd.components.settings.conversion-settings.conversion-row.view :refer [conversion-row]]
   [rdd.components.settings.conversion-settings.fx :as fx]
   [rdd.components.settings.conversion-settings.subs :as subs]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(defn conversion-editor
  [{:keys [id]}]
  (let [update-conversion (fn
                            [conversion-id key [val _]]
                            (rf/dispatch [:update-conversion conversion-id {key val}]))

        node-conversions @(rf/subscribe [:node-conversions id])

        uoms (rf/subscribe [:all-uoms])

        create-new-conversion (fn []
                                (rf/dispatch [:create-conversion id]))

        create-new-uom (fn [conversion-id key label]
                         (rf/dispatch [:create-and-link-uom-conversion conversion-id key label]))]


    [v-box
     :children [[v-box :children (for [conversion node-conversions]
                                   [conversion-row {:conversion conversion
                                                    :update-fn update-conversion
                                                    :create-uom-fn create-new-uom
                                                    :uoms uoms}])]

                [box
                 :class "mt-4"
                 :child [button
                         :label "Create new conversion +"
                         :on-click create-new-conversion]]]]))
