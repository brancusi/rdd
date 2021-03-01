(ns cljs.user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require
   [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                 find-doc print-doc pst source)]
   [clojure.pprint :refer (pprint)]
   [clojure.string :as str]
   [re-frame.core :as rf]))

(comment

  (rf/dispatch [:reset])
  (rf/dispatch [:reset-db-with {}])
  (js/console.log "Hello")


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

;;   (rf/dispatch [:update-edge 1 {:qty (rand-int 1000)}])
  )