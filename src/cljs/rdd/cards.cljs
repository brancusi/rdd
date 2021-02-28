(ns rdd.cards
  (:require
   [reagent.core :as r]
   [devcards.core :as dc :refer [defcard]]))


(defcard
  "This is a live interactive development environment using [Devcards](https://github.com/bhauman/devcards).
   You can use it to design, test, and think about parts of your app in isolation.
   
   The two 'cards' below show the two components in this app.")

(defn init []
  (dc/start-devcard-ui!))
