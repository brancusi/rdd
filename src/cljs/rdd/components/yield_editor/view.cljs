(ns rdd.components.yield-editor.view
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]
   [reagent.core  :as reagent]
   [goog.string :as gstring]
   [rdd.components.add-node-row.subs]
   [rdd.components.node-editor.subs]
   [rdd.components.node-editor.fx]
   [rdd.components.yield-editor.fx :as ye-fx]
   [rdd.components.inputs.uom-drop-down.view :refer [uom-drop-down]]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [goog.string.format]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [rdd.components.add-node-row.view :refer [add-node-editor]]
   [rdd.utils.conversions :refer []]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box]]))

(defn yield-editor
  [nt]
  (let [{:keys [id yield yield-uom children]} nt

        yield-uom-data @(rf/subscribe [:uom yield-uom])
        is-yield-uom-countable-type? (= :count (:type yield-uom-data))
        has-children? (seq children)

        sample-uom (if is-yield-uom-countable-type? "gram" yield-uom)
        sample-uom-data @(rf/subscribe [:uom sample-uom])

        normalized-children-weights (mapv (fn [{:keys [id uom qty]}]
                                            @(rf/subscribe [:weight-for-uom
                                                            id
                                                            uom
                                                            sample-uom
                                                            qty])) children)

        total-weight (reduce + (map js/parseFloat normalized-children-weights))
        yield-ratio (/ yield total-weight)
        has-yield-ratio? (not (js/Number.isNaN yield-ratio))

        update-node (fn
                      [key [val _]]
                      (rf/dispatch [:update-node id {key val}]))

        create-new-uom (fn [label]
                         (rf/dispatch [:create-and-link-uom-node id label]))]

    (if has-children?  [v-box
                        :class "my-4"
                        :children [[v-box :children [[h-box
                                                      :children [[label
                                                                  :label "Total measured weight of ingredients: "]
                                                                 [label
                                                                  :class "ml-2"
                                                                  :label (str total-weight)]

                                                                 [label
                                                                  :class "ml-2"
                                                                  :label (:label sample-uom-data)]]]

                                                     [h-box
                                                      :src (at)
                                                      :children [[:span [input-text
                                                                         :src (at)
                                                                         :model (str yield)
                                                                         :width "100px"
                                                                         :on-change #(update-node :yield [%])]]

                                                                 [uom-drop-down {:model yield-uom
                                                                                 :create-fn create-new-uom
                                                                                 :update-fn (partial update-node :yield-uom)}]]]
                                                     (when (not is-yield-uom-countable-type?)
                                                       [h-box
                                                        :children [[label
                                                                    :class "mr-2"
                                                                    :label "Yield ratio:"]

                                                                   [label
                                                                    :label (str (gstring/format "%.2f" (* 100 yield-ratio)) "%")]]])]]]]

        [v-box :children [[label
                           :label "Default UOM for this item"]
                          [uom-drop-down {:model yield-uom
                                          :create-fn create-new-uom
                                          :update-fn (partial update-node :yield-uom)}]]])))

