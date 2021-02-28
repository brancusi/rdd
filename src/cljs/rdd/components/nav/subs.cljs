(ns rdd.components.nav.subs
  (:require
   [re-frame.core :as rf :refer [subscribe]]))

(rf/reg-event-db
 :active-nav
 (fn [db _]
   (get-in db [:nav :active-nav])))
