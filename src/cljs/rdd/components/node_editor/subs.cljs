(ns rdd.components.node-editor.subs
  (:require
   [re-frame.core :as rf :refer [reg-sub]]))

(reg-sub
 :states
 (fn
   [db _]
   (:states db)))