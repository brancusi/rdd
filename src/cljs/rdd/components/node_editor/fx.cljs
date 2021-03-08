(ns rdd.components.node-editor.fx
  (:require
   [re-frame.core :as rf]))


(rf/reg-event-db
 :add-temp-node
 (fn
   [db [_ parent-node-id index]]
   (-> db
       (assoc-in [:states :node-editor] {:state :adding-node
                                         :node/id parent-node-id
                                         :previous-index index}))))