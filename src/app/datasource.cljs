(ns app.datasource
  (:require [reagent.core :as reagent :refer [atom]]))
c
(defonce wordlists
  (atom []
        )
)


(defn delete [vect item]
  (vec (remove (fn [word] (= word item)) vect)))

(defn delete!
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
                                   (assoc % :items (vec (concat (:items %) items))) 
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

