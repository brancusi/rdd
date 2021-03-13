(ns rdd.components.rows.node-row.view

  (:require
   [cljs.pprint :refer [pprint]]
   [taoensso.timbre :as timbre
    :refer-macros [info]]
   [rdd.components.add-node-row.subs]
   [rdd.components.rows.node-row.fx]
   [rdd.components.settings.view :refer [settings-panel]]
   [rdd.components.node-editor.subs]
   [rdd.components.node-editor.fx]
   [re-frame.core :as rf]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [herb.core :refer [<class]]
   [rdd.components.yield-editor.view :refer [yield-editor]]
   [rdd.components.quantity-editor.view :refer [quantity-editor]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap md-icon-button]]))

(defn row-style []
  {:padding-left "2rem"})

(defn node-row

  "Build a node row. Expects a node-tree and if this the top level node"

  [{:keys [tree
           parent-node-id]
    {:keys [id
            name
            state
            qty
            children
            uom
            index
            edge-id
            recipe-cost
            cost-per-uom]} :tree

    {:cost/keys [missing-cost?]} :tree

    {:scale/keys [local-qty]} :tree}]

  (let [create-edge #(rf/dispatch [:create-edge %1 %2 nil {:state {:type :new}}])
        destroy-edge #(rf/dispatch [:destroy-edge %1])
        is-new? (= (:type state) :new)]

    [v-box
     :class "border-2 my-1 py-2"
     :children [[h-box
                 :align :center
                 :children [(if is-new?
                              [add-node-editor {:edge-id edge-id
                                                :tree tree}]

                              [box
                               :size "150px"
                               :child [label
                                       :class "cursor-pointer"
                                       :label (str name " - " index)]])

                            (quantity-editor tree)

                            (when missing-cost?
                              [box
                               :class "ml-8"
                               :child [label
                                       :label (str "Cost missing")]])

                            [box
                             :class "ml-8"
                             :child [label
                                     :label (str "M-QTY: " qty " - " "L-QTY: " local-qty)]]


                            [box
                             :class "ml-8"
                             :child [button
                                     :label "+"
                                     :on-click #(create-edge parent-node-id index)]]

                            [box
                             :class "ml-8"
                             :child [label
                                     :label (str "RC: " recipe-cost " - " "UOM-C: " cost-per-uom)]]

                            [gap :size "1"]

                            [box
                             :class "mr-4"
                             :child [md-icon-button
                                     :md-icon-name "zmdi-settings"
                                     :tooltip      "Show settings"
                                     :size         :smaller
                                     :on-click #(rf/dispatch [:toggle-node-settings edge-id])]]

                            [box
                             :class "mr-4"
                             :child [button
                                     :label "X"
                                     :on-click #(destroy-edge edge-id)]]

                            ;; 
                            ]]

                ;; Display any custom panels based on state
                (case (:type state)
                  :settings [settings-panel id edge-id]
                  :other [:p "I'm a dope ass other"]
                  nil)

                (if-let [children children]
                  [v-box
                   :class (<class row-style)
                   :children [(for [child (sort-by :index children)]
                                (let [{:keys [edge-id]} child]
                                  ^{:key edge-id} [node-row
                                                   {:tree child
                                                    :parent-node-id id}
                                                   child]))]])]]))
