(ns app.datasource
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce words 
;  "Data model"
  (atom ["ClojureScript" "In the browser" "Review your notes" "Generate random associations" "Preview text materials" "React" "Reagent"]))


(defn delete [item]
  (swap! words #(vec (remove (fn [word] (= word item)) %))))

(defn clear []
 (reset! words []))
