(ns rdd.db
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [cljs.spec.gen.alpha :as gen]
            [taoensso.timbre :as timbre
             :refer-macros [log info spy]]
            [clojure.spec.alpha :as spec]))

(def spice-names
  ["Angelica"
   "Basil"
   "Holy Basil"
   "Bay leaf"
   "Indian Bay leaf"
   "Boldo"
   "Borage"
   "Chervil"
   "Chives"
   "Garlic Chives"
   "Cicely"
   "Coriander leaf"
   "Cilantro"
   "Bolivian Coriander"
   "Vietnamese Coriander"
   "Culantro"
   "Cress"
   "Curry leaf"
   "Dill"
   "Epazote"
   "Hemp"
   "Hoja santa"
   "Houttuynia cordata"
   "Hyssop"
   "Jimbu"
   "Kinh gioi"
   "Lavender"
   "Lemon balm"
   "Lemon grass"
   "Lemon myrtle"
   "Lemon verbena"
   "Limnophila aromatica"
   "Lovage"
   "Marjoram"
   "Mint"
   "Mugwort"
   "Mitsuba"
   "Oregano"
   "Parsley"
   "Perilla"
   "Rosemary"
   "Rue"
   "Sage"
   "Savory"
   "Sansho"
   "Shiso"
   "Sorrel"
   "Tarragon"
   "Thyme"
   "Woodruff"
   "Aonori"
   "Ajwain"
   "Allspice"
   "Amchoor"
   "Anise"
   "Star Anise"
   "Asafoetida"
   "Camphor"
   "Caraway"
   "Cardamom"
   "Black Cardamom"
   "Cassia"
   "Celery powder"
   "Celery seed"
   "Charoli"
   "Chenpi"
   "Cinnamon"
   "Clove"
   "Coriander seed"
   "Cubeb"
   "Cumin"
   "Nigella sativa"
   "Bunium persicum"
   "Dill"
   "Dill seed"
   "Fennel"
   "Fenugreek"
   "Fingerroot"
   "Greater Galangal"
   "Lesser Galangal"
   "Garlic"
   "Ginger"
   "Aromatic Ginger"
   "Golpar"
   "Grains of Paradise"
   "Grains of Selim"
   "Horseradish"
   "Juniper berry"
   "Kokum"
   "Korarima"
   "Dried Lime"
   "Liquorice"
   "Litsea cubeba"
   "Mace"
   "Mango-ginger"
   "Mastic"
   "Mahlab"
   "Black Mustard"
   "Brown Mustard"
   "White Mustard"
   "Nigella"
   "Njangsa"
   "Nutmeg"
   "Alligator Pepper"
   "Brazilian Pepper"
   "Chili Pepper"
   "Cayenne pepper"
   "Paprika"
   "Long Pepper"
   "Peruvian Pepper"
   "East Asian Pepper"
   "Sichuan Pepper"
   "Sansho"
   "Tasmanian Pepper"
   "Black Peppercorn"
   "Green Peppercorn"
   "White Peppercorn"
   "Pomegranate seed"
   "Poppy seed"
   "Radhuni"
   "Rose"
   "Saffron"
   "Salt"
   "Sarsaparilla"
   "Sassafras"
   "Sesame"
   "Shiso"
   "Sumac"
   "Tamarind"
   "Tonka bean"
   "Turmeric"
   "Uzazi"
   "Vanilla"
   "Voatsiperifery"
   "Wasabi"
   "Yuzu"
   "Zedoary"
   "Zereshk"
   "Zest"
   "Adjika"
   "Advieh"
   "Baharat"
   "Beau Monde seasoning"
   "Berbere"
   "Bouquet garni"
   "Buknu"
   "Chaat masala"
   "Chaunk"
   "Chili powder"
   "Crab boil"
   "Curry powder"
   "Doubanjiang"
   "Douchi"
   "Duqqa"
   "Fines herbes"
   "Five-spice powder"
   "Garam masala"
   "Garlic powder"
   "Garlic salt"
   "Gochujang"
   "Harissa"
   "Hawaij"
   "Herbes de Provence"
   "Idli podi"
   "Jamaican jerk spice"
   "Khmeli suneli"
   "Lemon pepper"
   "Mitmita"
   "Mixed spice"
   "Montreal steak seasoning"
   "Mulling spices"
   "Old Bay Seasoning"
   "Onion powder"
   "Panch phoron"
   "Persillade"
   "Powder-douce"
   "Pumpkin pie spice"
   "Qâlat daqqa"
   "Quatre épices"
   "Ras el hanout"
   "Recado rojo"
   "Sharena sol"
   "Shichimi"
   "Tabil"
   "Tandoori masala"
   "Vadouvan"
   "Yuzukosho"
   "Za'atar"])

(defn names->nodes
  [names]
  (reduce
   (fn [acc item]
     (assoc acc (:id item) item))
   {}
   (map
    (fn [name]
      {:name name
       :id (string/lower-case name)
       :yield 1
       :yield-uom "gram"})
    names)))

(comment
  (names->nodes (take 10 spice-names))
;;  
  )

(def standard-uoms {"cup" {:id "cup", :label "Cup", :system :imperial, :type :volume}
                    "floz" {:id "floz", :label "Fluid Ounce", :system :imperial, :type :volume}
                    "gallon" {:id "gallon", :label "Gallon", :system :imperial, :type :volume}
                    "gram" {:id "gram", :label "Gram", :system :metric, :type :weight}
                    "kilogram" {:id "kilogram", :label "Kilogram", :system :metric, :type :weight}
                    "ounce" {:id "ounce", :label "Ounce", :system :imperial, :type :weight}
                    "pound" {:id "pound", :label "Pound", :system :imperial, :type :weight}
                    "tbsp" {:id "tbsp", :label "Tablespoon", :system :imperial, :type :volume}
                    "tsp" {:id "tsp", :label "Teaspoon", :system :imperial, :type :volume}})

(def internal-nodes {"salt"         {:name "Salt"
                                     :id "salt"
                                     :yield 1
                                     :yield-uom "gram"
                                     :parent-edges ["sauce-1-salt"]
                                     :costs ["salt-cost-1"]
                                     :conversions ["salt-cup"]}

                     "thai-basil" {:name "Thai Basil"
                                   :id "thai-basil"
                                   :yield 1
                                   :yield-uom "gram"}

                     "pepper"       {:name "Pepper"
                                     :id "pepper"
                                     :yield 1
                                     :yield-uom "gram"
                                     :parent-edges ["sauce-1-pepper"]
                                     :costs ["pepper-cost-1"]}

                     "sauce-1"      {:name "Sauce 1"
                                     :id "sauce-1"
                                     :yield 1
                                     :yield-uom "gram"
                                     :child-edges ["sauce-1-salt" "sauce-1-pepper"]
                                     :parent-edges ["burrito-sauce-1"]}

                     "burrito"      {:name "Burrito"
                                     :id "burrito"
                                     :yield 1
                                     :yield-uom "burrito"
                                     :child-edges ["burrito-sauce-1"]}})
(def default-db
  {:selected-node "burrito"

   :nodes (merge (names->nodes (take 10 spice-names)) internal-nodes)

   :edges {"sauce-1-salt"         {:parent-node "sauce-1"
                                   :child-node "salt"
                                   :edge-id "sauce-1-salt"
                                   :qty 1
                                   :uom "gram"
                                   :index 1}

           "sauce-1-pepper"       {:parent-node "sauce-1"
                                   :child-node "pepper"
                                   :edge-id "sauce-1-pepper"
                                   :qty 1
                                   :uom "gram"
                                   :index 4}

           "burrito-sauce-1"  {:parent-node "burrito"
                               :child-node "sauce-1"
                               :edge-id "burrito-sauce-1"
                               :qty 1
                               :uom "gram"
                               :index 1}}

   :standard-uoms standard-uoms

   :custom-uoms {"burrito" {:id "burrito" :label "Burrito" :system :imperial :type :count}}

   :conversions {"salt-cup" {:id "salt-cup"
                             :from "cup"
                             :to "gram"
                             :factor 273}}

   :costs {"salt-cost-1"    {:id "salt-cost-1"
                             :cost 1
                             :qty 1
                             :uom "gram"
                             :date 1
                             :additional-cost 0}

           "pepper-cost-1"  {:id "pepper-cost-1"
                             :cost 1
                             :qty 1
                             :uom "gram"
                             :date 1
                             :additional-cost 0}}})

(defn seed-db
  []
  (rf/dispatch [:reset-db-with default-db]));