(ns app.imports.handler
  (:require [re-frame.core :refer [register-handler 
                                   after path
                                   trim-v debug]]
            [clojure.string :as str]
            [app.middleware :refer [channel-mw]]))



(defn tokenize-content [] 
  (concat (vec (for [page js/content]                 
                 (.split page ". ")))))

(def split-by-line
  (mapcat #(str/split % #"\n")))

(def trim-items 
  (map #(clojure.string/trim %)))

(def remove-empty ;; comp throws an error..
  (filter #(not= "" %)))

(def process-pipeline (comp split-by-line trim-items remove-empty))

(defn process-input [input]
  (let [in (if (sequential? input) input [input])] 
    (vec (distinct (sequence process-pipeline in)))))


;;; ---

(defn add-words [channels words channel] 
  (mapv #(if (= % channel)
           (assoc % :items (vec (concat (:items %) words))) 
           %) channels))

(register-handler
 :import
 channel-mw
 (fn [channels [text channel]]
   (add-words channels (process-input text) channel)))

(register-handler
 :words-add
 channel-mw
 (fn [channels [words channel]]    
   (add-words channels words channel)))


(defn tree->words [tree]
  (let [flattened (tree-seq map? #(:children %) tree)]
    (reduce #(conj %1 %2) [] flattened)))


;; TODO get the tree as clojure map from somewhere
(register-handler
 :tree-add
 channel-mw
 (fn [channels [tree channel]]
   (let [words (tree->words tree)]   
     (add-words channels (tree->words tree) channel))))

