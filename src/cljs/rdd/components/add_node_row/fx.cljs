(ns rdd.components.add-node-row.fx
  (:require
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [rdd.interceptors.db-interceptors :refer [re-link-edges re-index-edges]]))


(rf/reg-event-db
 :relink-child
 [re-index-edges]
 (fn
   [db [_ edge-id child-node-id state]]
   (let [edge (get-in db [:edges edge-id])
         node (get-in db [:nodes child-node-id])
         uom (or (:uom edge) (:yield-uom node))]
     (update-in db [:edges edge-id] merge {:child-node child-node-id
                                           :uom uom
                                           :state state}))))