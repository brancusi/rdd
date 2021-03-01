(ns rdd.subs
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [rdd.db :refer [default-db]]
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
 :cost
 (fn
   [db [_ id]]
   (get-in db [:costs id])))

(reg-sub
 :cost
 (fn
   [db [_ id]]
   (get-in db [:costs id])))

(reg-sub
 :cost-for-uom
 (fn [[_ node-id _]]
   [(subscribe [:cost node-id])
    (subscribe [:uoms])
    (subscribe [:conversion])])
 (fn
   [[cost uoms conversion] [_ _ to-uom]]))


(defn convert-uom->uom
  "Returns the conversion ratio from one UOM another"
  [from to]
  20)

(def standard-uoms
  {:default-weight-type :gram
   :default-volume-type :cup
   :default-unit-type :each

   :by-category {:weight [:gram :kilogram :pound]
                 :volume [:cup]
                 :count [:each]}

   :by-type {:gram      {:type :weight
                         :factor 1.0}

             :kilogram  {:type :weight
                         :factor 1000.0}

             :pound     {:type :weight
                         :factor 453.5}

             :cup       {:type :volume
                         :factor 1.0}

             :each      {:type :count}}
   :all-types [:gram :kilogram :pound :cup]})

(defn build-conversion-path
  [node-id from-uom to-uom]
  [])

(defn cost-for-uom
  [node-id cost conversion to-uom]

  ;; Normalize against base UOM
  (let [sorted-cost (sort-by :date cost)
        recent-cost (first sorted-cost)
        {:keys [cost uom qty]} recent-cost
        standard-uom (get-in standard-uoms [:by-type uom])]

    ;; Convert to target UOM
    ;; Try to find in standard uoms  
    (if (some? standard-uom)

      ;; This is a standard UOM can normalize
      (let [factor (:factor standard-uom)
            normalized-cost-per-uom (/ cost qty factor)]

        normalized-cost-per-uom)

      ;; Not a standard UOM, needs conversion
      20

      ;; 
      )

    ;; 
    ))


(def cost (get-in default-db [:costs "salt"]))
(def conversion (get-in default-db [:conversions "salt"]))
(def to-uom :gram)

(comment
  (/ 10 10 453)

  (cost-for-uom node-id cost conversion to-uom)

  ;; 
  )


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
      (if (not-empty children)

        ;; Has children
        (-> node
            (dissoc :child-edges)
            (merge {:children children}))

        ;; Terminate hit bottom
        (let [cost @(subscribe [:cost node-id])
              yo @(subscribe [:cost-for-uom node-id :gram])]
          (info "Yoson ")
          (-> node
              (merge {:cost yo}))))))))

;; Given an edge, we materialize a child node
(reg-sub-raw
 :edge->child
 (fn [_ [_ edge-id]]
   (reaction
    (let [edge @(subscribe [:edge edge-id])
          node @(subscribe [:node->tree (:child-node edge)])]

      (-> node
          (dissoc :child-edges)
          (merge edge))))))

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
