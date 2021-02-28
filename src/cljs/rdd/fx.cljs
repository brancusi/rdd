
(ns rdd.fx
  (:require

   ;; Utils
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
 :set-active-node
 (fn [{:keys [db]} [_ id]]
   {:db (assoc db :active-node id)}))

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
  [db uuid to qty]
  (assoc-in db [:edges uuid] {:child-node to :qty qty}))

(defn relate-edge
  [from uuid db]
  (->> (assoc-in db [:nodes from :child-edges] [uuid])))

(rf/reg-event-fx :add-child (fn [{:keys [db]} [_ from to qty]]
                              (let [uuid (nano-id)
                                    new-db (->> (add-edge db uuid to qty)
                                                (relate-edge from uuid))]
                                {:db new-db})))

(rf/reg-event-fx :reset (fn [_ _]
                          {:db default-db}))

(rf/reg-event-fx
 :update-active-route
 (fn [route]
   (info "Inside reg event" route)))