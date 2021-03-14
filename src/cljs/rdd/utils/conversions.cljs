(ns rdd.utils.conversions
  (:require-macros [clojure.string :as str])
  (:require [rdd.db :refer [default-db]]
            [cljs.pprint :refer [pprint]]
            [loom.graph :refer [graph nodes edges]]
            [loom.alg :refer [bf-path]]
            [taoensso.timbre :as timbre
             :refer-macros [info]]))

(def standard-uoms
  {:gram {:kilogram 0.001
          :pound 0.002204624
          :ounce 0.035273991}
   :tsp {:gallon 0.00130208
         :cup 0.0208333
         :tbsp 0.333333
         :floz 0.166667}})

(defn generate-reverse-lookups
  "Takes a conversion and generates all reverse lookups. 
   Returns the ammended map.

   {:pack {:gram 453}} -> {:pack {:gram 453} :gram {:pack 1}}
   
   Example:
   ```clojure
   (generate-reverse-lookups (get-in default-db [:conversions \" salt \"]))
   ```
   "
  [conversion]
  (reduce
   (fn
     [acc [from-uom val]]
     (let [output (reduce
                   (fn [acc [to-uom qty]]
                     (assoc-in acc [to-uom from-uom] (/ 1 qty)))
                   acc
                   val)]
       output))
   conversion
   (map (fn
          [[key val]]
          [key val])
        conversion)))

(defn uom->uom-factor
  "The factor when converting from one UOM to another
   
   (uom->uom-factor salt-conversions 20 :gram :pack)
   "
  [conversion qty from to]
  (if (= from to)
    qty
    (let [merged (merge conversion standard-uoms)
          reverse-lookup-index (generate-reverse-lookups merged)
          g (graph reverse-lookup-index)
          path (into [] (bf-path g from to))]
      (if (not-empty path)
        (let [{:keys [factor]}
              (reduce (fn
                        [acc next-hop]
                        (let [last-key (:last-key acc)
                              last-node (get-in reverse-lookup-index [last-key])
                              next-hop-factor (get-in last-node [next-hop])]

                          (if last-key
                          ;; Has last hop
                            (-> acc
                                (update :factor * next-hop-factor)
                                (assoc :last-key next-hop))

                          ;; Set defaults
                            (-> acc
                                (assoc :factor qty :last-key next-hop)))))
                      {:last-key nil :factor 1}
                      path)]
          (js/parseFloat factor))

      ;; No path found
        (str "No solution found for " qty from " to " to)))))



(defn cost-for-uom
  "The cost for a given UOM
   
   (cost-for-uom cost conversion :gram)"
  [cost conversion to-uom]

  ;; Normalize against base UOM
  (let [{:keys [cost additional-cost uom qty]} cost
        normalized-cost (/ (+ cost additional-cost) qty)
        factor (uom->uom-factor conversion 1 to-uom uom)]

    (* normalized-cost factor)))

(comment

  (def merged-conversions
    (merge
     (get-in default-db [:conversions "salt"])
     standard-uoms))

  (def son (generate-reverse-lookups merged-conversions))

  son
  ;; => {:case {:pack 25},
  ;;     :cup {:gram 10, :tsp 48.00007680012288},
  ;;     :floz {:tsp 5.999988000024},
  ;;     :gallon {:tsp 768.0019660850332},
  ;;     :gram {:cup 0.1, :kilogram 0.001, :ounce 0.035273991, :pinch 5, :pound 0.002204624},
  ;;     :kilogram {:gram 1000},
  ;;     :ounce {:gram 28.349499777328855},
  ;;     :pack {:case 0.04, :pound 1},
  ;;     :pinch {:gram 0.2},
  ;;     :pound {:gram 453.5920864510229, :pack 1},
  ;;     :tbsp {:tsp 3.0000030000030002},
  ;;     :tsp {:cup 0.0208333, :floz 0.166667, :gallon 0.00130208, :tbsp 0.333333}}


  (def supson (graph son))
  ;; => #'rdd.utils.conversions/supson



  (get-in default-db [:conversions "sauce-1"])
  ;; => {"burrito" {:gram 100}}

  (uom->uom-factor (get-in default-db [:conversions "salt"]) 1 :kilogram :pinch)

  (uom->uom-factor (get-in default-db [:conversions "sauce-1"]) 1 "burrito" :gram)

  (-> merged-conversions
      generate-reverse-lookups)

  (nodes supson)

  (edges supson)


  (bf-path supson :case :pinch)

  (into [] (bf-path supson :case :pinch))

  (* 20 0.002204624 1)

  (* 1 25 1 453 5)


  (take 5 (for
           [x (range 10)
            y (range 10)
            z (range 10)]
            (vec [x y z])))
  ;; 
  )
;; => ([0 0 0] [0 0 1] [0 0 2] [0 0 3] [0 0 4])

;; => ([0 0 0] [0 0 1] [0 0 2] [0 0 3] [0 0 4])

