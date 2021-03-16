(ns rdd.components.settings.conversion-settings.conversion-row.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [cljs.pprint :refer [pprint]]
   [rdd.components.inputs.uom-drop-down.view :refer [uom-drop-down]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(defn conversion-row
  [{:keys [conversion update-fn create-uom-fn uoms]}]
  (let [{:keys [id from to factor]} conversion]
    [h-box :children [[input-text
                       :src (at)
                       :model (str factor)
                       :on-change (fn [val _]
                                    (update-fn
                                     id
                                     :factor
                                     [(js/parseFloat val)]))]

                      [uom-drop-down {:model to
                                      :create-fn (partial create-uom-fn id :to)
                                      :update-fn (partial update-fn
                                                          id
                                                          :to)}]

                      [:p "per "]

                      [uom-drop-down {:model from
                                      :create-fn (partial create-uom-fn id :from)
                                      :update-fn (partial update-fn
                                                          id
                                                          :from)}]]]))
