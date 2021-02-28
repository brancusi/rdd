(ns rdd.components.node-editor.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reagent.core  :as reagent]
   [re-frame.core :as rf :refer [subscribe]]
   [herb.core :refer [<class]]
   [re-com.core   :refer [input-text button]]))

(defn style []
  {:color "red"
   :font-size "2rem"})

(defn row-style []
  {:padding-left "2rem"})

(defn node-row
  [nt]
  (let [id (:id nt)
        order (:order nt)
        name (:name nt)
        qty (:qty nt)
        children (:children nt)]
    [:div [:p
           {:class (<class style)}
           (str name " - " order " - " qty)]
     (if-let [children children]
       [:div
        {:class (<class row-style)}
        (for [child (sort-by :order children)]
          (let [id (:id child)
                order (:order child)
                key (str id "-" order)]
            ^{:key key} [node-row child]))])]))

(comment
  (rf/dispatch [:update-edge "sauce-1-salt" {:order 1}])
  (rf/dispatch [:set-active-node "sauce-1"])
  (rf/dispatch [:set-active-node "sauce-2"])
  (rf/dispatch [:set-active-node "burrito"])
  (rf/dispatch [:update-edge "sauce-1-salt" {:qty 20}])



  ;; 
  )

(defn node-editor
  [nt]
  [:div
   [node-row nt]])

