(ns rdd.utils.conversions
  (:require-macros [clojure.string :as str])
  (:require [rdd.db :refer [default-db]]
            [cljs.pprint :refer [pprint]]
            [loom.attr :refer [attr]]
            [loom.graph :refer [graph digraph weighted-graph weighted-digraph
                                nodes edges has-node? has-edge? transpose fly-graph
                                weight graph? Graph directed? Digraph weighted?
                                WeightedGraph subgraph add-path add-cycle]]
            [loom.alg :refer [pre-traverse post-traverse pre-span topsort
                              bf-traverse bf-span bf-path
                              bf-path-bi dijkstra-path dijkstra-path-dist
                              dijkstra-traverse dijkstra-span johnson
                              all-pairs-shortest-paths connected-components
                              connected? scc strongly-connected? connect
                              dag? shortest-path loners bellman-ford
                              bipartite-color bipartite? bipartite-sets
                              coloring? greedy-coloring prim-mst-edges
                              prim-mst-edges prim-mst astar-path astar-dist
                              degeneracy-ordering maximal-cliques
                              subgraph? eql? isomorphism?]]

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
  [conversion qty from to]
  (let [merged (merge conversion standard-uoms)
        reverse-lookup-index (generate-reverse-lookups merged)
        g (graph reverse-lookup-index)
        path (into [] (bf-path g from to))]
    (info path)
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
        factor)

      ;; No path found
      "No solution found")))

(def merged-conversions
  (merge
   (get-in default-db [:conversions "salt"])
   standard-uoms))

(def son (generate-reverse-lookups merged-conversions))
(def supson (graph son))

(comment

  (uom->uom-factor (get-in default-db [:conversions "salt"]) 25 :case :pinch)

  (-> merged-conversions
      generate-reverse-lookups
      pprint
      info)



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


  (nodes supson)

  (edges supson)
  ;; => ([:kilogram :gram]
  ;;     [:floz :tsp]
  ;;     [:gallon :tsp]
  ;;     [:ounce :gram]
  ;;     [:tbsp :tsp]
  ;;     [:gram :kilogram]
  ;;     [:gram :ounce]
  ;;     [:gram :pound]
  ;;     [:gram :cup]
  ;;     [:gram :pinch]
  ;;     [:pinch :gram]
  ;;     [:tsp :floz]
  ;;     [:tsp :gallon]
  ;;     [:tsp :tbsp]
  ;;     [:tsp :cup]
  ;;     [:pound :gram]
  ;;     [:pound :pack]
  ;;     [:case :pack]
  ;;     [:pack :pound]
  ;;     [:pack :case]
  ;;     [:cup :gram]
  ;;     [:cup :tsp])


  (into [] (bf-path supson :case :pinch))
  ;; => [:case :pack :pound :gram :pinch]


  (* 20 0.002204624 1)

  (* 1 25 1 453 5)


  ;; 
  )
