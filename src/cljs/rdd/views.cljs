(ns rdd.views
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [info]]
   [herb.core :refer [<class]]
   [re-frame.core :as rf :refer [subscribe]]

  ;;  Components
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.bom-row.view :refer [bom-row]]
   [rdd.components.node-editor.views.authenticated :refer [node-editor]]

   [re-com.core :as re-com :refer [at]]
   [herb.core :refer [<class]]
   [rdd.subs :as subs]))

(defn main-container-style []
  {:padding "2rem"})

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
  (rf/dispatch [:update-node node-id props]))


(defn main
  []
  (let [node-id @(subscribe [:selected-node-id])
        node @(subscribe [:selected-node])
        tree @(subscribe [:node->tree node-id 1])]
    [:<>
    ;;  [nav]
     [node-editor tree]
     [bom-row node-id node (partial update-node node-id)]
    ;;  [edn->hiccup @(subscribe [:db])]

    ;;  [edn->hiccup tree]
     ]))
    ;;  [edn->hiccup @(subscribe [:all])]]))
    ;; [edn->hiccup @(subscribe [:node->tree @(subscribe [:selected-node-id])])]

(defn main-panel []
  [re-com/v-box
   :class (<class main-container-style)
   :src      (at)
   :height   "100%"
   :children [[main]]])