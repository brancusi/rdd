(ns rdd.components.settings.cost-settings.cost-row.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.settings.cost-settings.fx :as fx]
   [cljs.pprint :refer [pprint]]
   [goog.date.Date]
   [cljs-time.core    :refer [today days minus plus day-of-week before?]]
   [cljs-time.coerce  :refer [to-local-date from-string to-long from-long]]
   [rdd.components.inputs.uom-drop-down.view :refer [uom-drop-down]]
   [cljs-time.format  :refer [formatter unparse]]
   [re-com.validate   :refer [date-like?]]
   [re-com.core   :refer [input-text button single-dropdown at v-box h-box label box gap datepicker-dropdown]]
   [taoensso.timbre :as timbre :refer-macros [log info spy]]
   [cljs.pprint :refer [pprint]]))

(defn- date->string
  [date]
  (if (date-like? date)
    (unparse (formatter "dd MMM, yyyy") date)
    "no date"))

(defn cost-row
  [{:keys [id cost qty uom date]}]
  (let [update-cost (fn
                      [key [val _]]
                      (rf/dispatch [:update-cost id {key val}]))

        create-new-uom (fn [label]
                         (rf/dispatch [:create-and-link-uom-cost id label]))]
    [h-box
     :src (at)
     :align :center
     :class "mt-2"
     :children [[input-text
                 :src (at)
                 :width "50px"
                 :model (str qty)
                 :on-change (fn [val] (update-cost :qty [val]))]

                [uom-drop-down {:model uom
                                :create-fn create-new-uom
                                :update-fn (partial update-cost :uom)}]

                [:span "="]
                [:span {:class "ml-4"} "$"]
                [input-text
                 :src (at)
                 :width "50px"
                 :model (str cost)
                 :on-change (fn [val] (update-cost :cost [val]))]


                [datepicker-dropdown
                 :src           (at)
                 :model         (when date (from-long date))
                 :show-today?   true
                 :show-weeks?   true
                 :placeholder   "Select a date"
                 :format        "MMM dd, yyyy"
                 :on-change     (fn [val]
                                  (update-cost :date [(to-long val)]))]]]))