(ns rdd.events
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.async-flow-fx :as async-flow-fx]
   [rdd.interceptors.db-interceptors :refer [from<-localstorage]]
   [rdd.db :as db]))

(defn boot-flow
  []
  {:first-dispatch [:load-from-ls]              ;; what event kicks things off ?
   :rules [;; a set of rules describing the required flow
           {:when :seen? :events :success-X  :dispatch [:do-Y]}
           {:when :seen? :events :success-Y  :dispatch [:do-Z]}
           {:when :seen? :events :success-Z  :halt? true}
           {:when :seen-any-of? :events [:fail-X :fail-Y :fail-Z] :dispatch [:app-failed-state] :halt? true}]})


(re-frame/reg-event-fx
 ::initialize-db
 [(from<-localstorage :db :stashed-db)]
 (fn [_ _]
   {:db db/default-db}))
