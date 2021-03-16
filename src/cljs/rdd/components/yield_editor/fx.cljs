(ns rdd.components.yield-editor.fx
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [rdd.interceptors.db-interceptors :refer [generate-uuid re-index-edges]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))


(rf/reg-event-fx
 :create-and-link-uom-node
 [rf/trim-v (generate-uuid :uom-id)]
 (fn [{:keys [db uom-id]} [node-id uom-label]]
   {:db db
    :fx [[:dispatch [:create-uom uom-id uom-label]]
         [:dispatch [:update-node node-id {:yield-uom uom-id}]]]}))