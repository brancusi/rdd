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
   [rdd.utils.conversions]
   [rdd.styles :as styles]

   ;;  Views
   [rdd.views :as views]
   [rdd.fx]
   [rdd.subs]))

(defn dev-setup []
  (when config/debug?
    (info "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (styles/inject-trace-styles js/document)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

(comment

  (rf/dispatch [:reset])
  (rf/dispatch [:reset-db-with {:active-node "burrito"

                                :editing/active-master-node "sauce-1"

                                :nodes {"salt"         {:name "Salt"
                                                        :id "salt"
                                                        :yield 1
                                                        :yield-uom :gram}

                                        "pepper"       {:name "Pepper"
                                                        :id "pepper"
                                                        :yield 1
                                                        :yield-uom :gram}

                                        "pepper2"       {:name "Pepper 2"
                                                         :id "pepper2"
                                                         :yield 1
                                                         :yield-uom :gram}

                                        "sauce-1"      {:name "Sauce 1"
                                                        :id "sauce-1"
                                                        :yield 1
                                                        :yield-uom :kilogram
                                                        :child-edges ["sauce-1-salt" "sauce-1-pepper" "sauce-1-pepper-2"]}

                                        "burrito"      {:name "Burrito"
                                                        :id "burrito"
                                                        :yield 1
                                                        :yield-uom "burrito"
                                                        :child-edges ["burrito-sauce-1"]}}

                                :edges {"sauce-1-salt"         {:child-node "salt"
                                                                :edge-id "sauce-1-salt"
                                                                :qty 10
                                                                :uom :gram
                                                                :order 1}

                                        "sauce-1-pepper"       {:child-node "pepper"
                                                                :edge-id "sauce-1-pepper"
                                                                :qty 10
                                                                :uom :gram
                                                                :order 2}

                                        "sauce-1-pepper-2"       {:child-node "pepper2"
                                                                  :edge-id "sauce-1-pepper-2"
                                                                  :qty 10
                                                                  :uom :gram
                                                                  :order 2}


                                        "burrito-sauce-1"  {:child-node "sauce-1"
                                                            :edge-id "burrito-sauce-1"
                                                            :qty 100
                                                            :uom :gram
                                                            :order 1}}

                                :conversions {"salt" {:cup    {:gram 10}
                                                      :case   {:pack 25}
                                                      :pinch  {:gram 0.2}
                                                      :pack   {:pound 1}}
                                              "sauce-1" {"burrito" {:gram 100}}}

                                :costs {"salt"    [{:cost 2
                                                    :qty 1
                                                    :uom :pound
                                                    :date 1
                                                    :additional-cost 0}]

                                        "pepper"  [{:cost 2
                                                    :qty 1
                                                    :uom :pound
                                                    :date 1
                                                    :additional-cost 0}]}}])

  ;; 
  )



{:active-node "burrito"

 :editing/active-master-node "sauce-1"

 :nodes {"salt"         {:name "Salt"
                         :id "salt"
                         :yield 1
                         :yield-uom :gram}

         "pepper"       {:name "Pepper"
                         :id "pepper"
                         :yield 1
                         :yield-uom :gram}

         "pepper2"       {:name "Pepper 2"
                          :id "pepper2"
                          :yield 1
                          :yield-uom :gram}

         "sauce-1"      {:name "Sauce 1"
                         :id "sauce-1"
                         :yield 1
                         :yield-uom :kilogram
                         :child-edges ["sauce-1-salt" "sauce-1-pepper" "sauce-1-pepper-2"]}

         "burrito"      {:name "Burrito"
                         :id "burrito"
                         :yield 1
                         :yield-uom "burrito"
                         :child-edges ["burrito-sauce-1"]}}

 :edges {"sauce-1-salt"         {:child-node "salt"
                                 :edge-id "sauce-1-salt"
                                 :qty 10
                                 :uom :gram
                                 :order 1}

         "sauce-1-pepper"       {:child-node "pepper"
                                 :edge-id "sauce-1-pepper"
                                 :qty 10
                                 :uom :gram
                                 :order 2}

         "sauce-1-pepper-2"       {:child-node "pepper2"
                                   :edge-id "sauce-1-pepper-2"
                                   :qty 10
                                   :uom :gram
                                   :order 2}


         "burrito-sauce-1"  {:child-node "sauce-1"
                             :edge-id "burrito-sauce-1"
                             :qty 100
                             :uom :gram
                             :order 1}}

 :conversions {"salt" {:cup    {:gram 10}
                       :case   {:pack 25}
                       :pinch  {:gram 0.2}
                       :pack   {:pound 1}}
               "sauce-1" {"burrito" {:gram 100}}}

 :costs {"salt"    [{:cost 2
                     :qty 1
                     :uom :pound
                     :date 1
                     :additional-cost 0}]

         "pepper"  [{:cost 2
                     :qty 1
                     :uom :pound
                     :date 1
                     :additional-cost 0}]}}
;; => {:active-node "burrito",
;;     :conversions {"salt" {:case {:pack 25}, :cup {:gram 10}, :pack {:pound 1}, :pinch {:gram 0.2}},
;;                   "sauce-1" {"burrito" {:gram 100}}},
;;     :costs {"pepper" [{:additional-cost 0, :cost 2, :date 1, :qty 1, :uom :pound}],
;;             "salt" [{:additional-cost 0, :cost 2, :date 1, :qty 1, :uom :pound}]},
;;     :edges {"burrito-sauce-1" {:child-node "sauce-1", :edge-id "burrito-sauce-1", :order 1, :qty 100, :uom :gram},
;;             "sauce-1-pepper" {:child-node "pepper", :edge-id "sauce-1-pepper", :order 2, :qty 10, :uom :gram},
;;             "sauce-1-pepper-2" {:child-node "pepper2", :edge-id "sauce-1-pepper-2", :order 2, :qty 10, :uom :gram},
;;             "sauce-1-salt" {:child-node "salt", :edge-id "sauce-1-salt", :order 1, :qty 10, :uom :gram}},
;;     :editing/active-master-node "sauce-1",
;;     :nodes {"burrito" {:child-edges ["burrito-sauce-1"], :id "burrito", :name "Burrito", :yield 1, :yield-uom "burrito"},
;;             "pepper" {:id "pepper", :name "Pepper", :yield 1, :yield-uom :gram},
;;             "pepper2" {:id "pepper2", :name "Pepper 2", :yield 1, :yield-uom :gram},
;;             "salt" {:id "salt", :name "Salt", :yield 1, :yield-uom :gram},
;;             "sauce-1" {:child-edges ["sauce-1-salt" "sauce-1-pepper" "sauce-1-pepper-2"],
;;                        :id "sauce-1",
;;                        :name "Sauce 1",
;;                        :yield 1,
;;                        :yield-uom :kilogram}}}
