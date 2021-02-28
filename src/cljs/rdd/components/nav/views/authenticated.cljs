(ns rdd.components.nav.views.authenticated
  (:require
   [taoensso.timbre :as timbre
    :refer-macros [log info spy]]
   [reitit.frontend.easy :as rtfe]))

(defn nav
  []
  (let [nav-items [{:id "builder" :name "Builder 1" :href :recipes}
                   {:id "settings" :name "Settings" :href :home}]]
    [:<>
     [:p "Main nav bar"]
     [:div (for [item nav-items]
             ^{:key (:id item)} [:a {:on-click (fn []
                                                 (info "href" (:href item))
                                                 (rtfe/push-state (:href item)))} (:name item)])]]))

