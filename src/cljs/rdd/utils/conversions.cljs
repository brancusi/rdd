(ns rdd.utils.conversions
  (:require-macros [clojure.string :as str])
  (:require [rdd.db :refer [default-db]]
            [cljs.pprint :refer [pprint]]
            [clojure.string :as str]
            [loom.graph :refer [graph nodes edges]]
            [loom.alg :refer [bf-path]]
            [taoensso.timbre :as timbre
             :refer-macros [info]]))

;; TODO: Rename to conversions, this is not to be used directly for uoms
(def uoms {:gram {:system :metric
                  :type :weight
                  :id :gram
                  :from :gram
                  :to :gram
                  :label "Gram"
                  :factor 1}

           :pound {:system :imperial
                   :type :weight
                   :id :pound
                   :from :pound
                   :to :gram
                   :label "Pound"
                   :factor 453.5920865}

           :ounce {:system :imperial
                   :type :weight
                   :id :ounce
                   :from :ounce
                   :to :gram
                   :label "Ounce"
                   :factor 28.34949978}

           :kilogram {:system :metric
                      :type :weight
                      :id :kilo
                      :from :kilo
                      :to :gram
                      :label "Kilogram"
                      :factor 1000}

           :tsp {:system :imperial
                 :type :volume
                 :id :tsp
                 :from :tsp
                 :to :tsp
                 :label "Teaspoon"
                 :factor 1}

           :gallon {:system :imperial
                    :type :volume
                    :id :gallon
                    :from :gallon
                    :to :tsp
                    :label "Gallon"
                    :factor 768.0019661}

           :cup {:system :imperial
                 :type :volume
                 :id :cup
                 :from :cup
                 :to :tsp
                 :label "Cup"
                 :factor 48.0000768}

           :tbsp {:system :imperial
                  :type :volume
                  :id :tbsp
                  :from :tbsp
                  :to :tsp
                  :label "Tablespoon"
                  :factor 3.000003}

           :floz {:system :imperial
                  :type :volume
                  :id :floz
                  :from :floz
                  :to :tsp
                  :label "Fluid Ounce"
                  :factor 5.999988}})

(defn uoms->grouped-by-type
  "Returns a vector of uoms grouped by type
   
   ```Clojure
   {:group :weight, :id :kilo, :label \"Kilogram\"}```
   
   "
  [col]
  (->> (mapv val col)
       (mapv (fn [{:keys [id type label]}]
               {:id id
                :label label
                :group (str/capitalize (name type))}))
       (sort-by :group)))

(defn generate-reverse-lookups
  "Takes a conversion and generates all reverse lookups. 
   Returns the ammended map.

   {:pack {:gram 453}} -> {:pack {:gram 453} :gram {:pack 1}}
   
   Example:
   ```clojure
   (generate-reverse-lookups (get-in default-db [:conversions \" salt \"]))
   ```
   "
  [col]
  (reduce
   (fn
     [acc [_ {:keys [from to factor]}]]
     (-> (assoc-in acc [from to] (/ 1 factor))
         (assoc-in [to from] factor)))
   {}
   col))

(defn merge-conversions
  [conversions]
  (reduce (fn [acc conversion] (merge acc conversion)) uoms conversions))

(defn uom->uom-factor
  "The factor when converting from one UOM to another
   
   (uom->uom-factor salt-conversions 20 :gram :pack)
   "
  [conversions qty from to]
  (if (= from to)
    qty
    (let [merged (merge-conversions conversions)
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
                                (update :factor / next-hop-factor)
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

  [{"salt-cup" (get-in default-db [:conversions "salt-cup"])}]
  ;; => {"salt-cup" {:factor 273, :from :cup, :id "salt-cup", :to :gram}}

  ;; => {:factor 273, :from :cup, :id "salt-cup", :to :gram}


  (def merged-conversions
    (merge-conversions
     [{"salt-cup" (get-in default-db [:conversions "salt-cup"])}]))


    ;; => {:cup {:factor 48.0000768, :from :cup, :id :cup, :label "Cup", :system :imperial, :to :tsp, :type :volume},
    ;;     :floz {:factor 5.999988, :from :floz, :id :floz, :label "Fluid Ounce", :system :imperial, :to :tsp, :type :volume},
    ;;     :gallon {:factor 768.0019661, :from :gallon, :id :gallon, :label "Gallon", :system :imperial, :to :tsp, :type :volume},
    ;;     :gram {:factor 1, :from :gram, :id :gram, :label "Gram", :system :metric, :to :gram, :type :weight},
    ;;     :kilogram {:factor 1000, :from :kilo, :id :kilo, :label "Kilogram", :system :metric, :to :gram, :type :weight},
    ;;     :ounce {:factor 28.34949978, :from :ounce, :id :ounce, :label "Ounce", :system :imperial, :to :gram, :type :weight},
    ;;     :pound {:factor 453.5920865, :from :pound, :id :pound, :label "Pound", :system :imperial, :to :gram, :type :weight},
    ;;     :tbsp {:factor 3.000003, :from :tbsp, :id :tbsp, :label "Tablespoon", :system :imperial, :to :tsp, :type :volume},
    ;;     :tsp {:factor 1, :from :tsp, :id :tsp, :label "Teaspoon", :system :imperial, :to :tsp, :type :volume},
    ;;     "salt-cup" {:factor 273, :from :cup, :id "salt-cup", :to :gram}}

    ;; => {:cup {:factor 48.0000768, :from :cup, :id :cup, :label "Cup", :system :imperial, :to :tsp, :type :volume},
    ;;     :factor 273,
    ;;     :floz {:factor 5.999988, :from :floz, :id :floz, :label "Fluid Ounce", :system :imperial, :to :tsp, :type :volume},
    ;;     :from :cup,
    ;;     :gallon {:factor 768.0019661, :from :gallon, :id :gallon, :label "Gallon", :system :imperial, :to :tsp, :type :volume},
    ;;     :gram {:factor 1, :from :gram, :id :gram, :label "Gram", :system :metric, :to :gram, :type :weight},
    ;;     :id "salt-cup",
    ;;     :kilogram {:factor 1000, :from :kilo, :id :kilo, :label "Kilogram", :system :metric, :to :gram, :type :weight},
    ;;     :ounce {:factor 28.34949978, :from :ounce, :id :ounce, :label "Ounce", :system :imperial, :to :gram, :type :weight},
    ;;     :pound {:factor 453.5920865, :from :pound, :id :pound, :label "Pound", :system :imperial, :to :gram, :type :weight},
    ;;     :tbsp {:factor 3.000003, :from :tbsp, :id :tbsp, :label "Tablespoon", :system :imperial, :to :tsp, :type :volume},
    ;;     :to :gram,
    ;;     :tsp {:factor 1, :from :tsp, :id :tsp, :label "Teaspoon", :system :imperial, :to :tsp, :type :volume}}

    ;; => {:cup {:factor 48.0000768, :from :cup, :id :cup, :label "Cup", :system :imperial, :to :tsp, :type :volume},
    ;;     :factor 273,
    ;;     :floz {:factor 5.999988, :from :floz, :id :floz, :label "Fluid Ounce", :system :imperial, :to :tsp, :type :volume},
    ;;     :from :cup,
    ;;     :gallon {:factor 768.0019661, :from :gallon, :id :gallon, :label "Gallon", :system :imperial, :to :tsp, :type :volume},
    ;;     :gram {:factor 1, :from :gram, :id :gram, :label "Gram", :system :metric, :to :gram, :type :weight},
    ;;     :id "salt-cup",
    ;;     :kilogram {:factor 1000, :from :kilo, :id :kilo, :label "Kilogram", :system :metric, :to :gram, :type :weight},
    ;;     :ounce {:factor 28.34949978, :from :ounce, :id :ounce, :label "Ounce", :system :imperial, :to :gram, :type :weight},
    ;;     :pound {:factor 453.5920865, :from :pound, :id :pound, :label "Pound", :system :imperial, :to :gram, :type :weight},
    ;;     :tbsp {:factor 3.000003, :from :tbsp, :id :tbsp, :label "Tablespoon", :system :imperial, :to :tsp, :type :volume},
    ;;     :to :gram,
    ;;     :tsp {:factor 1, :from :tsp, :id :tsp, :label "Teaspoon", :system :imperial, :to :tsp, :type :volume}}


  (def son (generate-reverse-lookups merged-conversions))

  son
  ;; => {:cup {:gram 0.003663003663003663, :tsp 0.02083330000005333},
  ;;     :floz {:tsp 0.16666700000066667},
  ;;     :gallon {:tsp 0.001302079999974625},
  ;;     :gram {:cup 273, :gram 1, :kilo 1000, :ounce 28.34949978, :pound 453.5920865},
  ;;     :kilo {:gram 0.001},
  ;;     :ounce {:gram 0.03527399099667641},
  ;;     :pound {:gram 0.0022046239997619535},
  ;;     :tbsp {:tsp 0.33333300000033333},
  ;;     :tsp {:cup 48.0000768, :floz 5.999988, :gallon 768.0019661, :tbsp 3.000003, :tsp 1}}

  ;; => {:cup {:tsp 0.02083330000005333},
  ;;     :floz {:tsp 0.16666700000066667},
  ;;     :gallon {:tsp 0.001302079999974625},
  ;;     :gram {:gram 1, :kilo 1000, :ounce 28.34949978, :pound 453.5920865},
  ;;     :kilo {:gram 0.001},
  ;;     :ounce {:gram 0.03527399099667641},
  ;;     :pound {:gram 0.0022046239997619535},
  ;;     :tbsp {:tsp 0.33333300000033333},
  ;;     :tsp {:cup 48.0000768, :floz 5.999988, :gallon 768.0019661, :tbsp 3.000003, :tsp 1}}

  ;; => {:kilogram {:system :metric, :type :weight, :id :kilo, :from :kilo, :to :gram, :label "Kilogram", :factor 1000}, :system {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :floz {:system :imperial, :type :volume, :id :floz, :from :floz, :to :tsp, :label "Fluid Ounce", :factor 5.999988}, :gallon {:system :imperial, :type :volume, :id :gallon, :from :gallon, :to :tsp, :label "Gallon", :factor 768.0019661}, :ounce {:system :imperial, :type :weight, :id :ounce, :from :ounce, :to :gram, :label "Ounce", :factor 28.34949978}, :type {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :tbsp {:system :imperial, :type :volume, :id :tbsp, :from :tbsp, :to :tsp, :label "Tablespoon", :factor 3.000003}, :gram {:system :metric, :type :weight, :id :gram, :from :gram, :to :gram, :label "Gram", :factor 1}, :tsp {:system :imperial, :type :volume, :id :tsp, :from :tsp, :to :tsp, :label "Teaspoon", :factor 1}, :from {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :pound {:system :imperial, :type :weight, :id :pound, :from :pound, :to :gram, :label "Pound", :factor 453.5920865}, :factor {:kilogram 0.001, :floz 0.16666700000066667, :gallon 0.001302079999974625, :ounce 0.03527399099667641, :tbsp 0.33333300000033333, :gram 1, :tsp 1, :pound 0.0022046239997619535, :cup 0.02083330000005333}, :label {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :id {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :cup {:system :imperial, :type :volume, :id :cup, :from :cup, :to :tsp, :label "Cup", :factor 48.0000768}, :to {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}}

  ;; => {:kilogram {:system :metric, :type :weight, :id :kilo, :from :kilo, :to :gram, :label "Kilogram", :factor 1000}, :system {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :floz {:system :imperial, :type :volume, :id :floz, :from :floz, :to :tsp, :label "Fluid Ounce", :factor 5.999988}, :gallon {:system :imperial, :type :volume, :id :gallon, :from :gallon, :to :tsp, :label "Gallon", :factor 768.0019661}, :ounce {:system :imperial, :type :weight, :id :ounce, :from :ounce, :to :gram, :label "Ounce", :factor 28.34949978}, "salt-cup" {:id "salt-cup", :from :cup, :to :gram, :factor 273}, :type {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :tbsp {:system :imperial, :type :volume, :id :tbsp, :from :tbsp, :to :tsp, :label "Tablespoon", :factor 3.000003}, :gram {:system :metric, :type :weight, :id :gram, :from :gram, :to :gram, :label "Gram", :factor 1}, :tsp {:system :imperial, :type :volume, :id :tsp, :from :tsp, :to :tsp, :label "Teaspoon", :factor 1}, :from {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, "salt-cup" ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :pound {:system :imperial, :type :weight, :id :pound, :from :pound, :to :gram, :label "Pound", :factor 453.5920865}, :factor {:kilogram 0.001, :floz 0.16666700000066667, :gallon 0.001302079999974625, :ounce 0.03527399099667641, "salt-cup" 0.003663003663003663, :tbsp 0.33333300000033333, :gram 1, :tsp 1, :pound 0.0022046239997619535, :cup 0.02083330000005333}, :label {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :id {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, "salt-cup" ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}, :cup {:system :imperial, :type :volume, :id :cup, :from :cup, :to :tsp, :label "Cup", :factor 48.0000768}, :to {:kilogram ##NaN, :floz ##NaN, :gallon ##NaN, :ounce ##NaN, "salt-cup" ##NaN, :tbsp ##NaN, :gram ##NaN, :tsp ##NaN, :pound ##NaN, :cup ##NaN}}

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

  (uom->uom-factor [{"salt-cup" (get-in default-db [:conversions "salt-cup"])}] 1 :cup :cup)

  (uom->uom-factor (get-in default-db [:conversions "sauce-1"]) 1 "burrito" :gram)

  (nodes supson)

  (edges supson)


  (bf-path supson :cup :gram)

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
