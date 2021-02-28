(ns rdd.subs
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
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
 :child-node
 (fn
   [db [_ edge]]
   (get-in db [:nodes (:child-node edge)])))

;; Given a node, we materialize a tree
(reg-sub-raw
 :node->tree
 (fn [_ [_ node-id]]
   (reaction
    (let [node @(subscribe [:node node-id])
          children (mapv (fn [edge-id] @(subscribe [:edge->child edge-id])) (:child-edges node))]
      (if (not (empty? children))

        ;; Has children
        (-> node
            (dissoc :child-edges)
            (merge {:children children}))

        ;; Terminate hit bottom
        node)))))

;; Given an edge, we materialize a child node
(reg-sub-raw
 :edge->child
 (fn [_ [_ edge-id]]
   (reaction
    (let [edge @(subscribe [:edge edge-id])
          node @(subscribe [:node->tree (:child-node edge)])
          qty (:qty edge)
          order (:order edge)]

      (-> node
          (dissoc :child-edges)
          (merge {:qty qty :order order}))))))

(comment
  (dissoc {:a 10 :b 20} :a)
  (->> 10
       (- 20))

  (-> 10
      (- 20))

  (->> {:a 10}
       (merge {:a 20}))

  (merge {:a 10} {:a 20})

  (-> {:a 10}
      (merge {:a 20}))

  (- 20 10)
  (- 10 20))



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
