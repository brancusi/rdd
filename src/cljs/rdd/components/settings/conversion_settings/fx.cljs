(ns rdd.components.settings.conversion-settings.fx
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.interceptors.db-interceptors :refer [generate-uuid]]
   [cljs.pprint :refer [pprint]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(rf/reg-event-fx
 :create-conversion
 [rf/trim-v (generate-uuid :conversion-id)]
 (fn [{:keys [db conversion-id]} [node-id]]
   {:db (-> db
            (assoc-in [:conversions conversion-id] {:id conversion-id})
            (update-in [:nodes node-id :conversions] conj conversion-id))}))

(rf/reg-event-fx
 :create-and-link-uom-conversion
 [rf/trim-v (generate-uuid :uom-id)]
 (fn [{:keys [db uom-id]} [conversion-id key uom-label]]
   {:db db
    :fx [[:dispatch [:create-uom uom-id uom-label]]
         [:dispatch [:update-conversion conversion-id {key uom-id}]]]}))