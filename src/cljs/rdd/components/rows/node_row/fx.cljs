(ns rdd.components.rows.node-row.fx
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(rf/reg-event-db
 :toggle-node-settings
 (fn [db [_ edge-id]]
   (let [{:keys [type panel]} (get-in db [:edges edge-id :state])
         open? (= type :settings)]
     (if open?
       (assoc-in
        db
        [:edges edge-id :state]
        {})
       (assoc-in
        db
        [:edges edge-id :state]
        {:type :settings
         :panel :cost})))))
