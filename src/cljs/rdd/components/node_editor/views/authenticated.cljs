(ns rdd.components.node-editor.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]
   [rdd.components.add-node-row.subs]
   [rdd.components.node-editor.subs]
   [rdd.components.node-editor.fx]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))


(defn row-style []
  {:padding-left "2rem"})

(defn qty-editor
  [nt]
  (let [{:keys [edge-id qty uom]} nt]
    [v-box
     :children [[h-box
                 :src (at)
                 :children [[:span

                             [input-text
                              :src (at)
                              :model (str qty)
                              :width "100px"
                              :on-change (fn [val]
                                           (dispatch [:update-edge edge-id {:qty val}]))]]
                            [single-dropdown
                             :src (at)
                             :model uom
                             :class "ml-2"
                             :choices [{:id :gram :label "Gram"}
                                       {:id :pound :label "Pound"}
                                       {:id :kilogram :label "Kilogram"}]
                             :on-change (fn [choice]
                                          (dispatch [:update-edge edge-id {:uom choice}]))]]]]]))


(defn yield-editor
  [nt]
  (let [{:keys [id yield yield-uom]} nt]
    [v-box
     :class "my-4"
     :children [[h-box
                 :src (at)
                 :children [[:span [input-text
                                    :src (at)
                                    :model (str yield)
                                    :width "100px"
                                    :on-change (fn [val]
                                                 (dispatch [:update-node id {:yield val}]))]]
                            [single-dropdown
                             :src (at)
                             :model yield-uom
                             :class "ml-2"
                             :choices [{:id :gram :label "Gram"}
                                       {:id :pound :label "Pound"}
                                       {:id :kilogram :label "Kilogram"}]
                             :on-change (fn [choice]
                                          (dispatch [:update-node id {:yield-uom choice}]))]]]]]))

(defn node-row

  "Build a node row. Expects a node-tree and if this the top level node"

  [{:keys [tree
           active-master-node
           parent-node-id
           states]
    {:keys [id name children uom index]} :tree
    {:scale/keys [local-qty]} :tree}]

  (let [is-active-master? (=  id (:id active-master-node))
        set-active-editor #(rf/dispatch [:set-active-master-node %])
        add-temp-node #(dispatch [:add-temp-node %1 %2])
        node-editor-state (:node-editor states)
        is-adding-node? (and
                         (= :adding-node (:state node-editor-state))
                         (= parent-node-id (:node/id node-editor-state))
                         (= index (:previous-index node-editor-state)))]

    [v-box
     :class "border-2 my-1 py-2"
     :children [[h-box
                 :align :center
                 :children [[box
                             :size "150px"
                             :child [label
                                     :on-click #(set-active-editor id)
                                     :class "cursor-pointer"
                                     :label (str name)]]

                            ;; TODO: Need to think about how to toggle this flag
                            (if true
                              (qty-editor tree)
                              nil)

                            [:span
                             {:class "ml-8"}
                             (str local-qty " - " uom " - Index - " index)]

                            [box
                             :class "ml-8"
                             :child [button
                                     :label "Add below"
                                     :on-click #(add-temp-node parent-node-id index)]]

                            ;; 
                            ]]

                (when is-adding-node?
                  [add-node-editor {:parent-node-id parent-node-id
                                    :tree tree}])

                (if-let [children children]
                  [v-box
                   :class (<class row-style)
                   :children [(for [child (sort-by :index children)]
                                (let [{:keys [edge-id]} child]
                                  ^{:key edge-id} [node-row

                                                   {:tree child
                                                    :parent-node-id id
                                                    :active-master-node active-master-node
                                                    :states states}

                                                   child
                                                   active-master-node
                                                   is-active-master?]))
                              (if is-active-master?
                                [h-box
                                 :align :center
                                 :children [[box
                                             :size "150px"
                                             :child "This recipe makes"]
                                            (yield-editor tree)

                                            ;; 
                                            ]]
                                nil)]])]]))


(defn node-editor
  [tree]
  (let [active-master-node @(subscribe [:active-master-node])
        states @(subscribe [:states])
        {:keys [id
                yield
                edge-id
                name
                qty
                children
                recipe-cost
                cost-per-uom
                yield-uom
                uom]} tree]
    [:div
     [:h1 {:class "my-8 text-4xl"} "Recipe editor"]
    ;;  [edn->hiccup active-master-node]

     [h-box
      :class "my-8"
      :children [[button
                  :label "Edit sauce"
                  :on-click (fn []
                              (rf/dispatch [:set-active-master-node "sauce-1"]))]
                 [button
                  :label "Edit burrito"
                  :on-click (fn []
                              (rf/dispatch [:set-active-master-node "burrito"]))]]]

     [node-row
      {:tree tree
       :active-master-node active-master-node
       :states states}]]))

