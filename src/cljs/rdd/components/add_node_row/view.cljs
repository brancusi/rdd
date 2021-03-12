(ns rdd.components.add-node-row.view
  (:require
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.db :refer [default-db]]
   [clojure.string :as string]
   [rdd.components.add-node-row.fx]
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [re-com.core :as re-com :refer [at typeahead]]
   [re-frame.core :as rf]))

(defn create-new-result
  [query]
  {:type :constructor
   :name (str "Create: " query)
   :value query})

(defn query->suggestions
  [col query]
  (for [[_ data] col
        :when (re-find
               (re-pattern
                (str "(?i)" query))
               (:name data))]
    (assoc data :type :add)))

(defn query->nodes
  [nodes query]
  (let [results (query->suggestions nodes query)]
    (if (seq results)
      (conj results (create-new-result query))
      [(create-new-result query)])))

(defn add-node-editor
  [{:keys [edge-id tree]}]
  (let [nodes @(rf/subscribe [:nodes])
        suggestions-for-search #(query->nodes nodes %)
        process-on-change (fn [{:keys [type] :as result}]
                            (case type
                              :constructor (rf/dispatch [:create-and-link-node-from-search-result result edge-id])
                              :add (rf/dispatch [:relink-child edge-id (:id result) :focused])))]

    ;; [edn->hiccup tree]

    [typeahead
     :src (at)
     :data-source suggestions-for-search
     :immediate-model-update? false
     :change-on-blur? true
     :on-change process-on-change
     :render-suggestion (fn [result]
                          [:span (:name result)])
     :suggestion-to-string (fn [result]
                             (:name result))]))

