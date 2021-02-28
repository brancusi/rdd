(ns rdd.core
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [info]]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [rdd.events :as events]
   [rdd.views :as views]
   [rdd.config :as config]

   ;;  Re-frame
   [rdd.subs]
   [rdd.fx]

   ;;  Utils
   [nano-id.core :refer [nano-id]]

  ;;  
   ))

(defn dev-setup []
  (when config/debug?
    (info "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn create-node
  [from-name]
  (let [uuid (nano-id)]
    (rf/dispatch [:add-node uuid {:name from-name}])
    uuid))

(defn create-tree
  [from-name to-name]
  (let [from-uuid (nano-id)
        to-uuid (nano-id)]
    (rf/dispatch [:add-node from-uuid {:name from-name}])
    (rf/dispatch [:add-node to-uuid {:name to-name}])
    (rf/dispatch [:add-child from-uuid to-uuid 2001])
    [from-uuid to-uuid]))

(defn create-sub-node
  [from-id new-name]
  (let [uuid (nano-id)]
    (rf/dispatch [:add-node uuid {:name new-name}])
    (rf/dispatch [:add-child from-id uuid 2001])
    [from-id uuid]))

(defn build-node-node
  ([] (do
        "Done"))
  ([from-id d w]
   (if (> d 0)
     (let [[_ to-id] (create-sub-node from-id (str "Sub-Node" (rand-int 10000)))]
       (build-node-node to-id (dec d) w))
     (build-node-node))))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

(comment

  (rf/dispatch [:reset])
  (rf/dispatch [:reset-db-with {}])

  ;; 
  )


;; (ns rdd.core
;;   (:require
;;    ;;  Utils
;;    [taoensso.timbre :as timbre
;;     :refer-macros [info]]
;;    [rdd.utils.seed :refer [seed-db]]
;;    [nano-id.core :refer [nano-id]]

;;    ;; Components
;;    [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
;;    [rdd.components.bom-row.view :refer [bom-row]]

;;    ;; Re-frame
;;    [re-frame.core :as rf :refer [subscribe]]
;;    [rdd.subs]
;;    [rdd.fx]
;;    [rdd.components.nav.views.authenticated :refer [nav]]

;;    ;; Routing
;;    [rdd.router :as router]

;;    ;;  React reagent
;;    [reagent.core :as r]
;;    [reagent.dom :as d]))

;; (defn create-node
;;   [from-name]
;;   (let [uuid (nano-id)]
;;     (rf/dispatch [:add-node uuid {:name from-name}])
;;     uuid))

;; (defn create-tree
;;   [from-name to-name]
;;   (let [from-uuid (nano-id)
;;         to-uuid (nano-id)]
;;     (rf/dispatch [:add-node from-uuid {:name from-name}])
;;     (rf/dispatch [:add-node to-uuid {:name to-name}])
;;     (rf/dispatch [:add-child from-uuid to-uuid 2001])
;;     [from-uuid to-uuid]))

;; (defn create-sub-node
;;   [from-id new-name]
;;   (let [uuid (nano-id)]
;;     (rf/dispatch [:add-node uuid {:name new-name}])
;;     (rf/dispatch [:add-child from-id uuid 2001])
;;     [from-id uuid]))

;; (defn build-node-node
;;   ([] (do
;;         "Done"))
;;   ([from-id d w]
;;    (if (> d 0)
;;      (let [[_ to-id] (create-sub-node from-id (str "Sub-Node" (rand-int 10000)))]
;;        (build-node-node to-id (dec d) w))
;;      (build-node-node))))

;; (comment
;;   (seed-db)

;;   (rf/dispatch [:reset])

;;   (let [[from to] (create-tree "Burrito" "Sauce")]
;;     (rf/dispatch [:set-active-node from]))

;;   (rf/dispatch [:set-active-node "salt"])
;;   (rf/dispatch [:set-active-node "burrito"])

;;   (rf/dispatch [:add-node (nano-id) {:name (str "Something " (rand-int 1000))}])

;;   (rf/dispatch [:add-child "burrito" "salt" 2001])

;;   (rf/dispatch [:update-node "burrito" {:name (str "Burritoy " (rand-int 1000))}])
;;   (rf/dispatch [:update-node "salt" {:name (str "Salty " (rand-int 1000))}])
;;   (rf/dispatch [:update-node "sauce-1" {:name (str "Saucey-1- " (rand-int 1000))}])
;;   (rf/dispatch [:update-node "sauce-2" {:name (str "Saucey-2- " (rand-int 1000))}])
;;   (rf/dispatch [:update-node "pepper" {:name (str "Peppery " (rand-int 1000))}])
;;   (rf/dispatch [:update-node "paprika" {:name (str "Paprikay " (rand-int 1000))}])

;;   (rf/dispatch [:update-edge 1 {:qty (rand-int 1000)}]))

;; (defn update-node
;;   [node-id props]
;;   (info node-id props)
;;   (rf/dispatch [:update-node node-id props]))

;; (defn app
;;   []
;;   (let [node-id (subscribe [:active-node-id])
;;         node (subscribe [:active-node])]
;;     [:<>
;;      [nav]
;;      [:p (str (:name @node))]
;;      [bom-row @node-id @node (partial update-node @node-id)]
;;      [edn->hiccup @(subscribe [:all])]]))
;;     ;; [edn->hiccup @(subscribe [:node->tree @(subscribe [:active-node-id])])]

;; (defn ^:dev/after-load start
;;   []
;;   (d/render [app]
;;             (.getElementById js/document "app")))

;; (defn ^:export init!
;;   []
;;   (router/start-routing!)
;;   (start))