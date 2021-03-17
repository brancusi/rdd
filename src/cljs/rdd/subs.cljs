(ns rdd.subs
  (:require-macros [clojure.string :as str])
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]
   [rdd.db :refer [default-db]]
   [cljs-time.core    :refer [today days minus plus day-of-week before? after?]]
   [cljs-time.coerce  :refer [to-local-date]]
   [cljs-time.format  :refer [formatter unparse parse]]
   [rdd.utils.conversions :refer [uom->uom-factor cost-for-uom uoms->grouped-by-type]]
   [re-frame.core :as rf :refer [subscribe reg-sub reg-sub-raw]]
   [reagent.ratom :as ra :refer [reaction]]))

(reg-sub
 :db
 (fn
   [db _]
   db))

(reg-sub
 :all-costs
 (fn
   [db _]
   (:costs db)))

(reg-sub
 :standard-uoms
 (fn
   [db _]
   (:standard-uoms db)))

(reg-sub
 :custom-uoms
 (fn
   [db _]
   (:custom-uoms db)))

(reg-sub
 :merged-uoms
 (fn [_]
   [(subscribe [:custom-uoms])
    (subscribe [:standard-uoms])])
 (fn
   [[custom-uoms standard-uoms]]
   (merge custom-uoms standard-uoms)))

(reg-sub
 :all-uoms
 :<- [:merged-uoms]
 (fn
   [all-uoms]
   (->> (uoms->grouped-by-type all-uoms)
        (mapv #(assoc % :type :select)))))

(reg-sub
 :uom
 :<- [:merged-uoms]
 (fn
   [uoms [_ uom-id]]

   (info uom-id uoms)
   (get uoms uom-id)))

(reg-sub
 :conversions
 (fn
   [db _]
   (:conversions db)))

(reg-sub
 :node-conversions
 (fn [[_ node-id]]
   [(subscribe [:conversions])
    (subscribe [:node node-id])])
 (fn
   [[all-conversions {:keys [conversions]}] _]
   (vec (keep all-conversions conversions))))

(reg-sub
 :nodes
 (fn
   [db _]
   (get-in db [:nodes])))

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
   (->> (get-in db [:nodes node-id :costs])
        (mapv #(get-in db [:costs %])))))

(reg-sub
 :sorted-node-cost
 (fn [[_ node-id]]
   [(subscribe [:node-costs node-id])])
 (fn
   [[node-costs] _]
   (sort-by :date node-costs)))

(reg-sub
 :recent-node-cost
 (fn [[_ node-id]]
   [(subscribe [:sorted-node-cost node-id])])
 (fn
   [[sorted-node-costs] _]
   (last sorted-node-costs)))

(reg-sub
 :weight-for-uom
 (fn [[_ node-id _ _ _]]
   [(subscribe [:node-conversions node-id])])
 (fn
   [[conversions] [_ node-id from-uom to-uom quantity]]

   (info node-id conversions)
   (uom->uom-factor conversions quantity from-uom to-uom)))

(reg-sub
 :cost-for-uom
 (fn [[_ node-id _]]
   [(subscribe [:recent-node-cost node-id])
    (subscribe [:node-conversions node-id])])
 (fn
   [[cost conversions] [_ _ uom]]
   (cost-for-uom cost conversions uom)))

(reg-sub
 :from-uom->uom
 (fn [[_ node-id]]
   [(subscribe [:node-conversions node-id])])
 (fn
   [[conversions] [_ node-id from-uom to-uom]]
   (info node-id conversions)
   (uom->uom-factor conversions 1 from-uom to-uom)))

(reg-sub
 :child-node
 (fn
   [db [_ edge]]
   (get-in db [:nodes (:child-node edge)])))

(reg-sub
 :selected-node-id
 (fn
   [db _]
   (:selected-node db)))

(reg-sub
 :selected-node
 (fn
   [db _]
   (get-in db [:nodes (:selected-node db)])))

(reg-sub
 :active-master-node-id
 (fn
   [db _]
   (get-in db [:editing :node/id])))

(reg-sub
 :active-master-node
 :<- [:nodes]
 :<- [:active-master-node-id]
 (fn
   [[nodes id] _]
   (get-in nodes [id])))

(defn sum-key [col key]
  (reduce (fn [acc child]
            (+ acc (key child)))
          0
          col))

;; Given a node, we materialize a tree
(reg-sub-raw
 :node->tree
 (fn [_ [_ node-id parent-qty parent-qty-uom]]
   (reaction
    (let [node @(subscribe [:node node-id])
          {:keys [yield yield-uom]} node
          children (mapv
                    (fn [edge-id]
                      @(subscribe [:edge->child edge-id yield yield-uom parent-qty parent-qty-uom]))
                    (:child-edges node))]
      (if (not-empty children)

        ;; Node has children 
        (let [raw-cost-per-uom (sum-key children :recipe-cost)
              {:keys [yield]} node
              cost-with-yield (/ raw-cost-per-uom yield)]
          (-> node
              (dissoc :child-edges)
              (merge {:cost-per-uom cost-with-yield
                      :recipe-cost raw-cost-per-uom
                      :children children})))

        ;; Terminate hit bottom node
        (let [{:keys [yield yield-uom]} node
              raw-cost-per-uom @(subscribe [:cost-for-uom node-id yield-uom])
              cost-with-yield (/ raw-cost-per-uom yield)]
          (-> node
              (merge {:cost-per-yield-uom cost-with-yield}))))))))

;; Given an edge, we materialize a child node
(reg-sub-raw
 :edge->child
 (fn [_ [_ edge-id parent-yield parent-yield-uom parent-qty parent-qty-uom]]
   (reaction
    (let [edge @(subscribe [:edge edge-id])
          {:keys [qty uom]} edge

          node @(subscribe [:node->tree (:child-node edge) qty uom])

          children (:children node)

          {:keys [id yield-uom]} node

          parent-local-yield-scale-factor @(subscribe [:from-uom->uom id parent-yield-uom uom])

          cost-per-uom @(subscribe [:cost-for-uom id uom])
          missing-cost? (js/Number.isNaN cost-per-uom)

          normalized-parent-qty (if parent-qty parent-qty 1)
          normalized-parent-qty-uom (if parent-qty-uom parent-qty-uom parent-yield-uom)
          parent-qty-yield-scale-factor @(subscribe [:from-uom->uom id normalized-parent-qty-uom parent-yield-uom])

          ;; TODO: Extract these out so we can test
          local-ratio (/ qty (* parent-yield parent-local-yield-scale-factor))
          local-qty (* normalized-parent-qty parent-local-yield-scale-factor parent-qty-yield-scale-factor local-ratio)]

      (if (empty? children)

        ;; This is the node level and has cost data
        (-> node
            (merge edge)
            (merge {:cost-per-uom cost-per-uom
                    :cost/missing-cost? missing-cost?
                    :scale/parent-yield-uom-local-uom-scale-factor parent-local-yield-scale-factor
                    :scale/parent-qty-uom-parent-yield-uom-scale-factor parent-qty-yield-scale-factor
                    :scale/local-ratio local-ratio
                    :scale/local-qty local-qty
                    :parent-yield parent-yield
                    :parent-qty normalized-parent-qty
                    :parent-qty-uom normalized-parent-qty-uom
                    :parent-yield-uom parent-yield-uom
                    :recipe-cost (* qty cost-per-uom)})
            (dissoc :child-edges :child-node))

        ;; This is the recipe level and there are no direct costs at
        ;; this level, instead we need to build from children
        (let [raw-cost-per-uom (sum-key children :recipe-cost)
              cost-factor @(subscribe [:from-uom->uom id yield-uom uom])
              {:keys [yield]} node
              cost-with-yield (/ raw-cost-per-uom yield)
              normalized-cost (/ (/ raw-cost-per-uom yield) cost-factor)]

          ;; Build and return new map
          (-> node
              (merge {:cost-per-uom normalized-cost
                      :scale/parent-yield-uom-local-uom-scale-factor parent-local-yield-scale-factor
                      :scale/parent-qty-uom-parent-yield-uom-scale-factor parent-qty-yield-scale-factor
                      :scale/local-ratio local-ratio
                      :scale/local-qty local-qty
                      :parent-yield parent-yield
                      :parent-qty normalized-parent-qty
                      :parent-qty-uom normalized-parent-qty-uom
                      :parent-yield-uom parent-yield-uom
                      :cost-per-yield-uom cost-with-yield
                      :recipe-cost (* qty normalized-cost)})
              (merge edge)
              (dissoc :child-edges :child-node))))))))