(ns rdd.components.settings.cost-settings.cost-row.view
  (:require
   [re-frame.core :as rf]
   [rdd.components.viewers.ednviewer :refer [edn->hiccup]]
   [rdd.components.settings.cost-settings.fx :as fx]
   [cljs.pprint :refer [pprint]]
   [goog.date.Date]
   [cljs-time.core    :refer [today days minus plus day-of-week before?]]
   [cljs-time.coerce  :refer [to-local-date from-string to-long from-long]]
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
  (let [update-cost (fn [merge-data]
                      (rf/dispatch [:update-cost id merge-data]))]
    [h-box
     :src (at)
     :align :center
     :children [[input-text
                 :src (at)
                 :width "50px"
                 :model (str qty)
                 :on-change (fn [val] (update-cost {:qty val}))]

                [single-dropdown
                 :src (at)
                 :class "mr-4"
                 :model uom
                 :width "100px"
                 :class "mr-4"
                 :choices [{:id :gram :label "Gram"}
                           {:id :pound :label "Pound"}
                           {:id :kilogram :label "Kilogram"}]
                 :on-change (fn [new-val] (update-cost {:uom new-val}))
                 #_(fn [choice]
                     (dispatch [:update-node id {:yield-uom choice}]))]

                [:span "="]
                [:span {:class "ml-4"} "$"]
                [input-text
                 :src (at)
                 :width "50px"
                 :model (str cost)
                 :on-change (fn [val] (update-cost {:cost val}))]


                [datepicker-dropdown
                 :src           (at)
                 :model         (when date (from-long date))
                 :show-today?   true
                 :show-weeks?   true
                 :placeholder   "Select a date"
                 :format        "dd MMM, yyyy"
                 :on-change     (fn [val]
                                  (update-cost {:date (to-long val)}))]]]))