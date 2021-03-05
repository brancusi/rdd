(ns rdd.components.node-editor.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))

(defn title-style []
  {:color "0x2B2B2B"
   :font-size "3rem"})

(defn style []
  {:color "red"
   :font-size "2rem"})

(defn row-style []
  {:padding-left "2rem"})

(defn pad-left [x]
  {:padding-left x})

(defn margin-right [x]
  {:margin-right x})

(defn margin-left [x]
  {:margin-left x})

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
  [nt active-master-node editing?]
  (let [{:keys [name children uom id]} nt
        {:scale/keys [local-qty]} nt
        is-active-master? (=  id (:id active-master-node))
        set-active-editor #(rf/dispatch [:set-active-master-node %])
        add-edge #(rf/dispatch [:add-child %1 %2])]
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
                            (if editing?
                              (qty-editor nt)
                              nil)


                            [:span
                             {:class "ml-8"}
                             (str local-qty " - " uom)]


                            [box
                             :class "ml-8"
                             :child [button
                                     :label "Add below"
                                     :on-click (fn [] (info "Clicked"))]]

                            [box
                             :class "ml-8"
                             :child [button
                                     :label "Pepper 2"
                                     :on-click #(add-edge id "pepper2")]]

                            [box
                             :class "ml-8"
                             :child [button
                                     :label "Pepper 3"
                                     :on-click #(add-edge id "pepper3")]]

                            [box
                             :class "ml-8"
                             :child [button
                                     :label "Pepper 4"
                                     :on-click #(add-edge id "pepper4")]]

                            ;; 
                            ]]

                (if-let [children children]
                  [v-box
                   :class (<class row-style)
                   :children [(for [child (sort-by :order children)]
                                (let [{:keys [id edge-id]} child]
                                  ^{:key edge-id} [node-row
                                                   child
                                                   active-master-node
                                                   is-active-master?]))
                              (if is-active-master?
                                [h-box
                                 :align :center
                                 :children [[box
                                             :size "150px"
                                             :child "This recipe makes"]
                                            (yield-editor nt)



                                            ;; 
                                            ]]
                                nil)]])]]))





(comment

  (rf/dispatch [:set-active-master-node "burrito"])
  (rf/dispatch [:set-active-master-node "sauce-1"])


  (rf/dispatch [:update-edge "sauce-1-salt" {:order 1}])
  (rf/dispatch [:update-edge "sauce-1-salt" {:order 1}])
  (rf/dispatch [:set-active-node "sauce-1"])

  (rf/dispatch [:set-active-node "sauce-2"])
  (rf/dispatch [:set-active-node "burrito"])
  (rf/dispatch [:update-edge "sauce-1-salt" {:qty 20}])

  ;; 
  )

(defn node-editor
  [nt]
  (let [active-master-node @(subscribe [:active-master-node])
        {:keys [id
                yield
                edge-id
                order
                name
                qty
                children
                recipe-cost
                cost-per-uom
                yield-uom
                uom]} nt]
    [:div
     [:h1 {:class "my-8 text-4xl"} "Recipe editor"]
     [edn->hiccup active-master-node]

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

     [node-row nt active-master-node]]))

