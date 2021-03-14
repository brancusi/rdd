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
   [reagent.core :as r]
   [re-frame.core :as rf]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [herb.core :refer [<class]]
   [rdd.components.yield-editor.view :refer [yield-editor]]
   [rdd.components.quantity-editor.view :refer [quantity-editor]]
   [re-com.core   :refer [input-text button single-dropdown at row-button v-box h-box label box gap md-icon-button]]))

(defn row-style []
  {:padding-left "2rem"})

(defn node-row
  "Build a node row. Expects a node-tree and if this the top level node"
  [_]
  (let [local-state (r/atom {:settings-open? false
                             :open? true
                             :panel :default})
        mouse-over (r/atom false)
        create-edge #(rf/dispatch [:create-edge %1 %2 nil {:state {:type :new}}])
        destroy-edge #(rf/dispatch [:destroy-edge %1])
        update-local-state #(swap! local-state merge %)
        toggle-open #(update-local-state {:open? (not (:open? @local-state))})
        toggle-settings-panel #(update-local-state {:settings-open? (not (:settings-open? @local-state))})]


    (fn [{:keys [tree
                 parent-node-id]
          {:keys [id
                  name
                  state
                  children
                  index
                  edge-id]} :tree

          {:cost/keys [missing-cost?]} :tree}]
      (let [is-new? (= (:type state) :new)
            has-children? (some? (seq children))]
        [v-box
         :children [[h-box
                     :class    "rc-div-table-row"
                     :attr {:on-mouse-over #(reset! mouse-over true)
                            :on-mouse-out #(reset! mouse-over false)}
                     :align :center
                     :children [(if is-new?
                                  [add-node-editor {:edge-id edge-id
                                                    :tree tree}]

                                  [h-box
                                   :class "mr-4"
                                   :width "150px"
                                   :children [(when has-children?
                                                [box
                                                 :class "mr-4"
                                                 :align :center
                                                 :child [md-icon-button
                                                         :md-icon-name (if (:open? @local-state) "zmdi-chevron-down" "zmdi-chevron-right")
                                                         :size         :smaller
                                                         :on-click toggle-open]])


                                              [box
                                               :size "150px"
                                               :child [label
                                                       :class "cursor-pointer"
                                                       :label (str name " - " index)]]]])

                                (quantity-editor tree)

                                (when missing-cost?
                                  [box
                                   :class "ml-8"
                                   :child [label
                                           :label (str "Cost missing")]])

                                [gap :size "1"]

                                [box
                                 :class "mr-4"
                                 :child [row-button
                                         :md-icon-name "zmdi-long-arrow-down"
                                         :mouse-over-row? @mouse-over
                                         :tooltip      "Add below"

                                         :on-click #(create-edge parent-node-id index)]]

                                [box
                                 :class "mr-16"
                                 :child [row-button
                                         :md-icon-name "zmdi-long-arrow-return"
                                         :mouse-over-row? @mouse-over
                                         :tooltip      "Add inside"

                                         :on-click #(create-edge id 1)]]

                                [box
                                 :class "mr-4"
                                 :child [row-button
                                         :md-icon-name "zmdi-settings"
                                         :mouse-over-row? @mouse-over
                                         :tooltip      "Show settings"

                                         :on-click toggle-settings-panel]]

                                [box
                                 :class "mr-4"
                                 :child [row-button
                                         :md-icon-name "zmdi-delete"
                                         :mouse-over-row? @mouse-over
                                         :tooltip      "Delete row"
                                         :on-click #(destroy-edge edge-id)]]]]

                    (when (:settings-open? @local-state)

                      [settings-panel @local-state update-local-state id edge-id])

                    (when (:open? @local-state)
                      (when has-children?
                        [v-box
                         :class (<class row-style)
                         :children [(for [child (sort-by :index children)]
                                      (let [{:keys [edge-id]} child]
                                        ^{:key edge-id} [node-row
                                                         {:tree child
                                                          :parent-node-id id}]))]]))]]))))
