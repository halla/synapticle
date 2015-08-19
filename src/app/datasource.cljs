(ns app.datasource
  (:require [reagent.core :as reagent :refer [atom]]))





#_(defn delete-wordlist [wordlist]
  (swap! wordlists #(vec (remove (fn [list] (= list wordlist)) %))))

#_(defn add-wordlist [title]
  (swap! wordlists #(conj % {:title title :items []})))

