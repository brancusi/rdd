(ns rdd.core
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [info]]
   [reagent.dom :as rdom]

   ;;  Re-frame
   [rdd.subs]
   [re-frame.core :as rf]
   [rdd.events :as events]

   ;;  Utils
   [rdd.config :as config]

   ;;  Views
   [rdd.views :as views]

   [rdd.fx]))

(defn dev-setup []
  (when config/debug?
    (info "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

(comment

  (rf/dispatch [:reset])
  (rf/dispatch [:reset-db-with {}])

  ;; 
  )
