(ns app.datasource
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce wordlists
  (atom [{:title "List 1"
          :items ["Collide ideas"
                  "Review your notes" 
                  "Generate random associations" 
                  "Preview text materials" 
                  "Computer assisted thinking" 
                  "Immerse yourself on a subject"
                  "Just relax and watch the screen"
                  "Let your thoughts wander"                  
                  "If you get an idea, add it to the wordlist"
                  "You can pause the screen to hold a thought"]}
         {:title "Expand"
          :items ["imagine" 
                  "possible"
                  "future"]}
         {:title "Questions"
          :items ["What if?" "What else?"]}]
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
   (let [wordlist (@wordlists wordlist-idx)]
     (swap! wordlists (fn [list]
                        (mapv #(if (= % wordlist)
                                 (assoc % :items (delete (:items %) item)) 
                                 %) list))))))

(defn add-multiple!
  ([wordlist items]
   (swap! wordlists (fn [list]
                      (vec (map #(if (= % wordlist)
                                   (assoc % :items (concat (:items %) items)) 
                                   %) list)))))
  ([items] 
   (add-multiple! (@wordlists 0) items)))

(defn add!  
  ([wordlist item]
   (swap! wordlists (fn [list]
                      (vec (map #(if (= % wordlist)
                                   (assoc % :items (conj (:items %) item)) 
                                   %) list)))))
  ([item] 
   (add! (@wordlists 0) item))
)

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
