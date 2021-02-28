(ns rdd.views
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [info]]
   [re-frame.core :as rf :refer [subscribe]]


  ;;  Components
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.bom-row.view :refer [bom-row]]

   [re-com.core :as re-com :refer [at]]
   [herb.core :refer [<class]]
   [rdd.subs :as subs]))

(defn style []
  {:background "grey"})

(defn title []
  (let [name (rf/subscribe [::subs/name])]
    [re-com/title
     :src   (at)
     :label (str "Hello from " @name)
     :class (<class style)
     :level :level1]))

(defn update-node
  [node-id props]
  (info node-id props)
  (rf/dispatch [:update-node node-id props]))


(defn node-viewer
  []
  (let [node-id (subscribe [:active-node-id])
        node (subscribe [:active-node])]
    [:<>
    ;;  [nav]
     (info "Hey")
     [:p (str (:name @node) "!!!!")]
     [bom-row @node-id @node (partial update-node @node-id)]
     [edn->hiccup @(subscribe [:all])]

     [edn->hiccup @(subscribe [:node->tree @(subscribe [:active-node-id])])]]))
    ;;  [edn->hiccup @(subscribe [:all])]]))
    ;; [edn->hiccup @(subscribe [:node->tree @(subscribe [:active-node-id])])]

(defn main-panel []
  [re-com/v-box
   :src      (at)
   :height   "100%"
   :children [[title] [node-viewer]]])