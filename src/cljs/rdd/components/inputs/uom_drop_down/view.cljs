(ns rdd.components.inputs.uom-drop-down.view
  (:require
   [re-frame.core :as rf]
   [cljs.pprint :refer [pprint]]
   [clojure.string :as string]
   [re-com.core   :refer [single-dropdown at]]
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

(defn uom-drop-down
  [{:keys [model
           update-fn
           create-fn]}]
  (let [uoms @(rf/subscribe [:all-uoms])
        current-val (->> uoms
                         (filter #(= model (:id %)))
                         first)]
    [:div
     ^{:key (count uoms)} [single-dropdown
                           :src (at)
                           :width "150px"
                           :filter-box? true
                           :auto-complete? true
                           :model current-val
                           :label-fn (fn [val] (:label val))
                           :id-fn (fn [val] val)
                           :debounce-delay 0
                           :on-change (fn [val]
                                        (case (:type val)
                                          :create (create-fn (:val val))
                                          :select (update-fn [(:id val) val])))
                           :choices (fn [{:keys [filter-text]} done _]
                                      (let [results (query->suggestions uoms :label filter-text)]
                                        (done (if (seq results)
                                                results
                                                [{:label (str "No match found. Create " filter-text) :val filter-text :type :create}]))))]]))
