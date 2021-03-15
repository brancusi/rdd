(ns rdd.components.settings.conversion-settings.conversion-editor
  (:require
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [clojure.string :as string]
   [rdd.utils.conversions :as conversions :refer [uoms->grouped-by-type]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))


(defn query->suggestions
  "Query collect by key based on query"
  [col key query]
  (for [item col
        :when (re-find
               (re-pattern
                (str "(?i)" (string/lower-case query)))
               (string/lower-case (get item key)))]
    item))
(get {:id 20} :id)

(defn conversion-editor
  [{:keys [id edge-id] :as tree}]
  (let [a (reagent/atom "")

        update-conversion (fn [conversion-id opts] (rf/dispatch [:update-conversion conversion-id opts]))

        choices (->> (uoms->grouped-by-type conversions/uoms)
                     (mapv #(assoc % :type :select)))
                ;; => [{:group "Volume", :id :floz, :label "Fluid Ounce", :type :select}
                ;;     {:group "Volume", :id :gallon, :label "Gallon", :type :select}
                ;;     {:group "Volume", :id :tbsp, :label "Tablespoon", :type :select}
                ;;     {:group "Volume", :id :tsp, :label "Teaspoon", :type :select} {:group "Volume", :id :cup, :label "Cup", :type :select}
                ;;     {:group "Weight", :id :kilo, :label "Kilogram", :type :select}
                ;;     {:group "Weight", :id :ounce, :label "Ounce", :type :select} {:group "Weight", :id :gram, :label "Gram", :type :select}
                ;;     {:group "Weight", :id :pound, :label "Pound", :type :select}]

        node-conversions @(rf/subscribe [:node-conversions id])
        create-new-conversion (fn []
                                #_(rf/dispatch [:create-relate-cost id {:cost 1
                                                                        :qty 1
                                                                        :uom :pound
                                                                        :additional-cost 0}]))]
    [v-box
     :children [[v-box :children (for [conversion node-conversions]
                                   (let [{:keys [id from to factor]} conversion
                                         from-choice (->> choices
                                                          (filter #(= from (:id %)))
                                                          first)
                                         to-choice (->> choices
                                                        (filter #(= to (:id %)))
                                                        first)]

                                     [h-box :children [[input-text
                                                        :src (at)
                                                        :model (str factor)
                                                        :on-change #(update-conversion id {:factor (js/parseFloat %)})]

                                                       [single-dropdown
                                                        :src (at)
                                                        :width "150px"
                                                        :filter-box? true
                                                        :auto-complete? true
                                                        :model to-choice
                                                        :label-fn (fn [val] (:label val))
                                                        :id-fn (fn [val] val)
                                                        :debounce-delay 0
                                                        :on-change (fn [val]
                                                                     (case (:type val)
                                                                       :create (info "Create it")
                                                                       :select (update-conversion id {:to (:id val)})))
                                                        :choices (fn [{:keys [filter-text]} done _]
                                                                   (let [results (query->suggestions choices :label filter-text)]
                                                                     (done (if (seq results)
                                                                             results
                                                                             [{:label "Add it +" :type :create}]))))]

                                                       [:p "per "]
                                                       [single-dropdown
                                                        :src (at)
                                                        :width "150px"
                                                        :filter-box? true
                                                        :auto-complete? true
                                                        :model from-choice
                                                        :label-fn (fn [val] (:label val))
                                                        :id-fn (fn [val] val)
                                                        :debounce-delay 0
                                                        :on-change (fn [val]
                                                                     (case (:type val)
                                                                       :create (info "Create it")
                                                                       :select (update-conversion id {:from (:id val)})))
                                                        :choices (fn [{:keys [filter-text]} done _]
                                                                   (let [results (query->suggestions choices :label filter-text)]
                                                                     (done (if (seq results)
                                                                             results
                                                                             [{:label "Add it +" :type :create}]))))]

                                                       #_[single-dropdown
                                                          :src (at)
                                                          :width "150px"
                                                          :filter-box? true
                                                          :auto-complete? true
                                                          :model from
                                                          :debounce-delay 0
                                                          :id-fn :id
                                                          :on-change (fn [val]
                                                                       (case (:type val)
                                                                         :create (info "Create it")
                                                                         :select (reset! a val)))
                                                          :choices (fn [{:keys [filter-text]} done _]
                                                                     (let [results (->> (query->suggestions choices :label filter-text)
                                                                                        (mapv #(assoc % :type :select)))]
                                                                       (done (if (seq results)
                                                                               results
                                                                               [{:label "Add it +" :type :create}]))))]]]
                                    ;;  
                                     )
                                ;;    
                                   )]

                [box
                 :class "mt-4"
                 :child [button
                         :label "Create new conversion +"
                         :on-click create-new-conversion]]]]))
