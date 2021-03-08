(ns clj.user
  (:require
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
   [loom.io :refer [view]]))


(comment

  (def supson {:case {:pack 25}
               :cup {:gram 10}
               :floz {:tsp 5.999988000024}
               :gallon {:tsp 768.0019660850332}
               :gram {:cup 0.1, :kilogram 0.001, :ounce 0.035273991, :pound 0.002204624}
               :kilogram {:gram 1000}
               :ounce {:gram 28.349499777328855}
               :pack {:case 0.04, :pound 1}
               :pound {:gram 453.5920864510229, :pack 1}
               :tbsp {:tsp 3.0000030000030002}
               :tsp {:floz 0.166667, :gallon 0.00130208, :tbsp 0.333333}})

  (def son (graph supson))

  (view son)


  (nodes son)
  ;; => #{:case :cup :floz :gallon :gram :kilogram :ounce :pack :pound :tbsp :tsp}

  (edges son)
  (bf-path son :kilogram :case)
  (bf-path son :kilogram :tsp)

;;   
  )