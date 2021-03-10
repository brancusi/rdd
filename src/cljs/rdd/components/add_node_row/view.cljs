(ns rdd.components.add-node-row.view
  (:require
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.db :refer [default-db]]
   [rdd.components.add-node-row.fx]
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [re-com.core :as re-com :refer [at typeahead]]
   [re-frame.core :as rf]))


(defn query->nodes
  [seq query]
  (let [hey (into []
                  (take 16
                        (for [item seq
                              :when (re-find (re-pattern (str "(?i)" query))
                                             (:name (last item)))]
                          (last item))))]


    hey))


;; (for [item (:nodes default-db)
;;       :when 
;;       (re-find (re-pattern (str "(?i)" "s")) (:name item))]
;;   item)

;; (for [item (:nodes default-db)
;;       :when (do
;;               (js/console.log (last item))
;;               (= "Salt" (:name (last item))))]
;;   item)


;; (query->key (:nodes default-db) [:name] "s")

(defn add-node-editor
  [{:keys [edge-id tree]}]
  (let [nodes @(rf/subscribe [:nodes])
        suggestions-for-search #(query->nodes nodes %)]

    ;; [edn->hiccup tree]

    [typeahead
     :src (at)
     :data-source suggestions-for-search
     :immediate-model-update? false
     :change-on-blur? true
     :on-change (fn [{:keys [id] :as node}]
                  (when id
                    (do (info "called on change" edge-id node)
                        (rf/dispatch [:relink-child edge-id (:id node) :focused]))))
     :render-suggestion (fn [result]
                          [:span (:name result)])
     :suggestion-to-string (fn [result]
                             (:name result))]))

