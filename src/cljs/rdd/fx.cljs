
(ns rdd.fx
  (:require-macros [clojure.string :as str])
  (:require
   [cljs.pprint :refer [pprint]]
   [taoensso.timbre :as timbre
    :refer-macros [info]]

   [rdd.db :refer [default-db]]
   [rdd.interceptors.db-interceptors :refer [generate-uuid re-index-edges]]
   [cljs-time.core    :refer [today days minus plus day-of-week before?]]
   [cljs-time.coerce  :refer [to-local-date from-string to-long from-long]]

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
 :update-conversion
 [rf/trim-v]
 (fn [{:keys [db]} [id data]]
   (let [merged-data (merge data)]
     {:db (update-in
           db
           [:conversions id]
           merge
           merged-data)})))

(rf/reg-event-fx
 :create-cost
 [rf/trim-v]
 (fn [{:keys [db]} [cost-id data]]
   (let [cost-data (merge data {:id cost-id
                                :uom "pound"
                                :date (to-long (today))})]
     {:db (assoc-in
           db
           [:costs cost-id]
           cost-data)})))

(rf/reg-event-fx
 :relate-cost
 [rf/trim-v]
 (fn [{:keys [db]} [node-id cost-id]]
   {:db (update-in
         db
         [:nodes node-id :costs]
         conj
         cost-id)}))

(rf/reg-event-fx
 :create-uom
 [rf/trim-v]
 (fn [{:keys [db]} [uom-id label opts]]
   {:db (assoc-in
         db
         [:custom-uoms uom-id]
         (merge
          {:id uom-id
           :label label
           :type :count}
          opts))}))

(rf/reg-event-fx
 :create-relate-cost
 [rf/trim-v (generate-uuid :cost-id)]
 (fn [{:keys [db cost-id]} [node-id cost-data]]
   {:db db
    :fx [[:dispatch [:create-cost cost-id cost-data]]
         [:dispatch [:relate-cost node-id cost-id]]]}))

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

(defn relate-edge
  [from uuid db]
  (update-in
   db
   [:nodes from :child-edges]
   #(conj % uuid)))

(defn unrelate-edge
  [from uuid db]
  (update-in
   db
   [:nodes from :child-edges]
   #(conj % uuid)))


(defn remove-item
  [to-remove data]
  (remove #(= to-remove %) data))

(def test-db {:nodes {"sauce" {:id "sauce"
                               :child-edges ["sauce-salt" "b" "c"]}
                      "salt" {:id "salt"
                              :parent-edges ["sauce-salt" "b" "c"]}}
              :edges {"sauce-salt" {:edge-id "sauce-salt"
                                    :parent-node "sauce"
                                    :child-node "salt"}}})

(defn remove-in
  "Safely try to remove an item from a vector in a nested map.
   If the path to the vector returns nil, it will just return the original data"
  [path to-remove data]
  (if (seq (get-in data path))
    (update-in
     data
     path
     (fn [data]
       (vec (remove #(= to-remove %) data))))
    data))

(defn remove-edge-relationships
  [db edge-id]
  (let [edge (get-in db [:edges edge-id])
        parent-node (get-in db [:nodes (:parent-node edge)])
        parent-node-id (:id parent-node)
        child-node (get-in db [:nodes (:child-node edge)])
        child-node-id (:id child-node)]
    (->> db
         (remove-in [:nodes parent-node-id :child-edges] edge-id)
         (remove-in [:nodes child-node-id :parent-edges] edge-id))))

(rf/reg-event-fx
 :destroy-edge
 [rf/trim-v]
 (fn [{:keys [db]} [edge-id]]
   {:db (-> db
            (remove-edge-relationships edge-id)
            (update-in [:edges] dissoc edge-id))}))

(rf/reg-event-fx
 :create-edge
 [rf/trim-v (generate-uuid :edge-id) re-index-edges]
 (fn [{:keys [db edge-id]} [parent-id index child-id opts]]
   (let [child-node (get-in db [:nodes child-id])
         {:keys [yield-uom]} child-node
         opts (if opts opts {})]
     {:edge-id edge-id
      :db (->>
           (assoc-in db [:edges edge-id] (merge {:child-node child-id
                                                 :parent-node parent-id
                                                 :qty 1
                                                 :uom yield-uom
                                                 :edge-id edge-id
                                                 :index index} opts))

           (relate-edge parent-id edge-id))})))

(rf/reg-event-fx :reset (fn [_ _]
                          {:db default-db}))

(rf/reg-event-fx
 :update-active-route
 (fn [route]
   (info "Inside reg event" route)))