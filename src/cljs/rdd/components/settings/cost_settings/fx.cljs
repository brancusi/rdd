(ns rdd.components.settings.cost-settings.fx
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(rf/reg-event-db
 :update-cost
 [rf/trim-v]
 (fn [db [cost-id merge-data]]
   (info cost-id merge-data)
   (update-in db [:costs cost-id] merge merge-data)))