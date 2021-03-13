(ns rdd.components.add-node-row.fx
  (:require
   [re-frame.core :as rf]
   [rdd.interceptors.db-interceptors :refer [generate-uuid]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [rdd.interceptors.db-interceptors :refer [re-link-edges re-index-edges]]))

(rf/reg-event-db
 :relink-child
 [rf/trim-v]
 (fn
   [db [edge-id child-node-id state]]
   (let [edge (get-in db [:edges edge-id])
         node (get-in db [:nodes child-node-id])
         uom (or (:uom edge) (:yield-uom node))]
     (update-in db [:edges edge-id] merge {:child-node child-node-id
                                           :uom uom
                                           :state state}))))

(rf/reg-event-db
 :create-node
 [rf/trim-v]
 (fn [db [node-id node-data]]
   (assoc-in db [:nodes node-id] node-data)))

(defn search-result->node-data
  [new-node-id {:keys [value]}]
  {:id new-node-id
   :name value
   :yield 1
   :yield-uom :gram})

(rf/reg-event-fx
 :create-and-link-node-from-search-result
 [rf/trim-v (generate-uuid :node-id)]
 (fn [{:keys [node-id]} [search-result edge-id]]
   (let [node-data (search-result->node-data node-id search-result)]
     {:fx [[:dispatch [:create-node node-id node-data]]
           [:dispatch [:relink-child edge-id node-id {:type :focused}]]]})))