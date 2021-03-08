(ns rdd.components.add-node-row.subs
  (:require
   [re-frame.core :as rf :refer [subscribe reg-sub reg-sub-raw]]))


(reg-sub
 :editing-settings
 (fn [db _]
   (get-in db [:editing])))