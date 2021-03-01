(ns rdd.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:body {:color "red"}]
  [:.level1 {:color "green"}])

(+ 2 10)