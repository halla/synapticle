(ns app.datasource
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce wordlists
  (atom [{:title "List 1"
          :items ["ClojureScript" "In the browser" "Review your notes" "Generate random associations" "Preview text materials" "React" "Reagent"]}
         {:title "Colors"
          :items ["red" "green" "blue" "yellow" "black" "white"]}
         {:title "Questions"
          :items ["Why?" "How?" "Why not?" "How exactly?" "Where?" "Where else?" "Who?" "Who else?" "When?"]}]
        )
)
(defonce words 
;  "Data model"
  (atom ["ClojureScript" "In the browser" "Review your notes" "Generate random associations" "Preview text materials" "React" "Reagent"]))


(defn delete [vect item]
  (vec (remove (fn [word] (= word item)) vect)))

(defn delete!
  ([item]
   (swap! words #(delete % item)))
  ([wordlist-idx item] 
   (swap! words #(update-in % [wordlist-idx] (fn [list] {:title (:title list)
                                                         :items (delete (:items list) item)})))))

(defn delete-wordlist [wordlist]
  (swap! wordlists #(vec (remove (fn [list] (= list wordlist)) %))))

(defn add-wordlist [title]
  (swap! wordlists #(conj % {:title title :items []})))

(defn clear 
  ([] (reset! words []))
  ([wordlist] (swap! wordlists (fn [list]
                                 (vec (map #(if (= % wordlist)
                                               (assoc % :items []) 
                                               %) list))))))
