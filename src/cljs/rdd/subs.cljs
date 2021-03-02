(ns rdd.subs
  (:require-macros [clojure.string :as str])
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]
   [rdd.db :refer [default-db]]
   [rdd.utils.conversions :refer [uom->uom-factor cost-for-uom]]
   [re-frame.core :as rf :refer [subscribe reg-sub reg-sub-raw]]
   [reagent.ratom :as ra :refer [reaction]]))

(reg-sub
 ::name
 (fn [_]
   "Hi there"))

(reg-sub
 :all
 (fn
   [db _]
   db))

(reg-sub
 :all-costs
 (fn
   [db _]
   (:costs db)))

(reg-sub
 :uoms
 (fn
   [db _]
   (:uoms db)))

(reg-sub
 :all-conversions
 (fn
   [db _]
   (:conversions db)))

(reg-sub
 :conversion
 :<- [:all-conversions]
 (fn
   [conversions [_ id]]
   (get-in conversions [id])))

(reg-sub
 :node
 (fn
   [db [_ id]]
   (get-in db [:nodes id])))

(reg-sub
 :edge
 (fn
   [db [_ id]]
   (get-in db [:edges id])))

(reg-sub
 :node-costs
 (fn
   [db [_ node-id]]
   (get-in db [:costs node-id])))

(reg-sub
 :recent-node-cost
 (fn [[_ node-id]]
   [(subscribe [:node-costs node-id])])
 (fn
   [[node-costs] _]
   (last (sort-by :date node-costs))))

(reg-sub
 :cost-for-uom
 (fn [[_ node-id]]
   [(subscribe [:recent-node-cost node-id])
    (subscribe [:conversion node-id])])
 (fn
   [[cost conversion] [_ _ uom]]
   (cost-for-uom cost conversion uom)))

(reg-sub
 :from-uom->uom
 :<- [:conversion]
 (fn
   [[conversion] [_ _ from-uom to-uom]]
   (uom->uom-factor conversion 1 from-uom to-uom)))

(comment
  (def cost (get-in default-db [:costs "salt"]))
  (def conversion (get-in default-db [:conversions "salt"]))
  (def to-uom :gram)

  (/ 10 10 453)

  (uom->uom-factor conversion 10 :pound :pound)

  cost

  (cost-for-uom cost conversion :pound)

  (rf/dispatch [:set-active-node "sauce-1"])
  (rf/dispatch [:set-active-node "burrito"])

  (rf/dispatch [:add-node-cost "salt" {:cost 1
                                       :qty 1
                                       :uom :pound
                                       :date 100007
                                       :additional-cost 0}])

  ;; 
  )

(reg-sub
 :child-node
 (fn
   [db [_ edge]]
   (get-in db [:nodes (:child-node edge)])))

(defn cost-of-children [col]
  (reduce (fn [acc {:keys [total-cost]}]
            (+ acc total-cost))
          0
          col))

;; Given a node, we materialize a tree
(reg-sub-raw
 :node->tree
 (fn [_ [_ node-id]]
   (reaction
    (let [node @(subscribe [:node node-id])
          children (mapv (fn [edge-id] @(subscribe [:edge->child edge-id])) (:child-edges node))]
      (if (not-empty children)

        ;; Has children
        (let [raw-cost-per-uom (cost-of-children children)
              {:keys [yield]} node
              cost-with-yield (/ raw-cost-per-uom yield)]
          (-> node
              (dissoc :child-edges)
              (merge {:cost-per-uom cost-with-yield
                      :recipe-cost raw-cost-per-uom
                      :total-cost raw-cost-per-uom
                      :children children})))

        ;; Terminate hit bottom
        (let [{:keys [yield default-uom]} node
              raw-cost-per-uom @(subscribe [:cost-for-uom node-id default-uom])
              cost-with-yield (/ raw-cost-per-uom yield)]
          (-> node
              (merge {:cost-per-default-uom cost-with-yield}))))))))

;; Given an edge, we materialize a child node
(reg-sub-raw
 :edge->child
 (fn [_ [_ edge-id]]
   (reaction
    (let [edge @(subscribe [:edge edge-id])
          {:keys [qty uom]} edge
          node @(subscribe [:node->tree (:child-node edge)])
          children (:children node)
          {:keys [id default-uom]} node
          cost-per-uom @(subscribe [:cost-for-uom id uom])]

      (if (not (js/Number.isNaN cost-per-uom))
        (-> node
            (merge edge)
            (merge {:cost-per-uom cost-per-uom
                    :total-cost (* qty cost-per-uom)})
            (dissoc :child-edges)
            (dissoc :child-node))

        (let [raw-cost-per-uom (cost-of-children children)
              factor @(subscribe [:from-uom->uom id default-uom uom])
              {:keys [yield]} node
              cost-with-yield (/ raw-cost-per-uom yield)
              normalized-cost (/ (/ raw-cost-per-uom yield) factor)]
          (-> node
              (merge {:cost-per-uom normalized-cost
                      :cost-per-default-uom cost-with-yield
                      :recipe-cost normalized-cost
                      :total-cost (* qty normalized-cost)})
              (merge edge)
              (dissoc :child-edges)
              (dissoc :child-node))))))))

(reg-sub
 :active-node-id
 (fn
   [db _]
   (:active-node db)))

(reg-sub
 :active-node
 (fn
   [db _]
   (get-in db [:nodes (:active-node db)])))
