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
 (fn [[_ node-id]]
   [(subscribe [:conversion node-id])])
 (fn
   [[conversion] [_ _ from-uom to-uom]]
   (uom->uom-factor conversion 1 from-uom to-uom)))

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
          children (mapv (fn [edge-id] @(subscribe [:edge->child edge-id yield yield-uom parent-qty parent-qty-uom])) (:child-edges node))]
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
          {:keys [id yield-uom name]} node

          parent-local-yield-scale-factor @(subscribe [:from-uom->uom id parent-yield-uom uom])

          cost-per-uom @(subscribe [:cost-for-uom id uom])
          normalized-parent-qty (if parent-qty parent-qty 1)
          normalized-parent-qty-uom (if parent-qty-uom parent-qty-uom parent-yield-uom)

          parent-qty-yield-scale-factor @(subscribe [:from-uom->uom id normalized-parent-qty-uom parent-yield-uom])

          local-ratio (/ qty (* parent-yield parent-local-yield-scale-factor))

          local-qty (* normalized-parent-qty parent-local-yield-scale-factor parent-qty-yield-scale-factor local-ratio)]

      (if (not (js/Number.isNaN cost-per-uom))

        ;; This is the node level and has cost data
        (-> node
            (merge edge)
            (merge {:cost-per-uom cost-per-uom

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
        ;; @TODO - We should just check if this is a childless node and 
        ;; branch based on that instead of this NaN check.
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

(comment
  (dissoc {:a 10 :b 20} :a :b)
  (def cost (get-in default-db [:costs "salt"]))
  (def conversion (get-in default-db [:conversions "salt"]))
  (def to-uom :gram)

  (/ 10 10 453)

  (uom->uom-factor conversion 10 :pound :pound)

  cost

  (cost-for-uom cost conversion :pound)

  (rf/dispatch [:set-selected-node "sauce-1"])
  (rf/dispatch [:set-selected-node "burrito"])

  (rf/dispatch [:add-node-cost "salt" {:cost 1
                                       :qty 1
                                       :uom :pound
                                       :date 100007
                                       :additional-cost 0}])

  ;; 
  )




{:local/yield 1
 :parent/parent-qty :gram
 :edge/edge-id "sauce-1-pepper"
 :local/name "Pepper"
 :local/cost-per-yield-uom 0.004409248
 :parent/parent-qty-uom :gram
 :local/recipe-cost 0.04409248
 :scale/parent-yield-uom-local-uom-scale-factor 1000
 :local/cost-per-uom 0.004409248
 :local/uom :gram
 :scale/local-ratio 0.01
 :local/id "pepper"
 :local/yield-uom :gram
 :local/qty 10
 :local/index 2
 :parent/parent-yield-uom :kilogram
 :parent/parent-yield 1}