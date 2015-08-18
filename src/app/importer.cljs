(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch-sync]]
            [app.ctl :as ctl]
            [clojure.string :as str]
            [cljs.core.async :refer [put!]]))


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


(defn keydownhandler [nextword active-list-idx]
  (fn [e]
    (when (= (.-which e) 27) ;esc
      (reset! nextword ""))
    (when (= (.-key e) "Enter")
      (dispatch-sync [:words-add (process-input @nextword)])
      (reset! nextword ""))))

(defn textfield-component [wordstore]
  (let [active-list-idx (subscribe [:active-list-idx])
        nextword (atom "")]
    (fn []
      [:input {:type "text" 
               :value @nextword
               :placeholder (str "Add item to " (:title (@wordstore @active-list-idx)))
               :class "form-control"
               :on-change #(reset! nextword (-> % .-target .-value))
               :on-key-down (keydownhandler nextword active-list-idx)}])))
