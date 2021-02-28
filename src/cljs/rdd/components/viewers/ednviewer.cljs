(ns rdd.components.viewers.ednviewer
  (:require-macros [clojure.string :as str])
  (:require
   [cljs.pprint :refer [pprint]]))

(defn edn->hiccup [edn]
  [:pre
   [:code (with-out-str (pprint edn))]])

