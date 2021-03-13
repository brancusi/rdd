(ns rdd.components.settings.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [rdd.components.settings.cost-settings.view :refer [cost-settings]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))


(defn settings-panel
  [node-id edge-id]
  (let [node @(rf/subscribe [:node node-id])
        edge @(rf/subscribe [:edge edge-id])]
    (when true
      [cost-settings node edge])))
