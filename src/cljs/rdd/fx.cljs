
(ns rdd.fx
  (:require-macros [clojure.string :as str])
  (:require
   [cljs.pprint :refer [pprint]]
   [taoensso.timbre :as timbre
    :refer-macros [info]]

   [rdd.db :refer [default-db]]

   [re-frame.core :as rf]
   [nano-id.core :refer [nano-id]]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(rf/reg-event-db
 :reset-db-with
 (fn [_ [_ data]]
   data))

(rf/reg-event-fx
 :set-selected-node
 (fn [{:keys [db]} [_ id]]
   {:db (assoc db :selected-node id)}))

(rf/reg-event-fx
 :set-active-master-node
 (fn [{:keys [db]} [_ id]]
   {:db (assoc-in db [:editing :node/id] id)}))

(rf/reg-event-fx
 :add-node-cost
 (fn [{:keys [db]} [_ node-id cost]]
   {:db (update-in
         db
         [:costs node-id]
         #(conj % cost))}))

(rf/reg-event-fx
 :update-node
 (fn [{:keys [db]} [_ id fields]]
   {:db (update-in db [:nodes id] merge fields)}))

(rf/reg-event-fx
 :update-edge
 (fn [{:keys [db]} [_ id fields]]
   {:db (update-in db [:edges id] merge fields)}))

(rf/reg-event-fx
 :add-node
 (fn [{:keys [db]} [_ id fields]]
   {:db (assoc-in db [:nodes id] fields)}))

(defn add-edge
  [db uuid {:keys [to uom qty]}]
  (assoc-in db [:edges uuid] {:child-node to :qty qty :uom uom :edge-id uuid :index 1}))

(defn relate-edge
  [from uuid db]
  (update-in
   db
   [:nodes from :child-edges]
   #(conj % uuid)))

(rf/reg-event-fx
 :add-child
 (fn [{:keys [db]} [_ parent-id child-id]]
   (let [edge-id (nano-id)
         child-node (get-in db [:nodes child-id])
         {:keys [yield-uom]} child-node]
     {:db (->> (add-edge db edge-id {:from parent-id
                                     :to child-id
                                     :uom yield-uom
                                     :qty 1})
               (relate-edge parent-id edge-id))})))

{:child-node "salt"
 :edge-id "sauce-1-salt"
 :qty 10
 :uom :gram
 :index 1}

(rf/reg-event-fx :reset (fn [_ _]
                          {:db default-db}))

(rf/reg-event-fx
 :update-active-route
 (fn [route]
   (info "Inside reg event" route)))