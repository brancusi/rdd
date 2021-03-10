(ns rdd.utils.db-utils
  (:require
   [rdd.db :refer [default-db]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]))

(defn edge-siblings
  [db edge-id]
  (let [edge (get-in db [:edges edge-id])
        parent-node (get-in db [:nodes (:parent-node edge)])
        sibling-edges-ids (:child-edges parent-node)]
    (map (fn [edge-id]
           (get-in db [:edges edge-id]))
         sibling-edges-ids)))

(edge-siblings default-db "sauce-1-salt")