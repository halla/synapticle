(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]))


(defn tokenize-content []
  (concat (vec (for [page js/content]                 
                 (.split page ". ")
                 ))))

(defn textarea-import [words]
  (let [ws (js->clj (.split (.-value (.getElementById js/document "textareaimport")) "\n"))]
    (println ws)
    (reset! words (js->clj ws))))


;; single word input field

(defonce nextword (atom ""))

(defn textfield-import [words word]
  (swap! words conj (js->clj word))
  (reset! nextword ""))


(defn keydownhandler [wordstore word]
  (fn [e]
    (when (= (.-which e) 27) ;esc
      (reset! nextword ""))
    (when (= (.-key e) "Enter")
      (textfield-import wordstore word))))

(defn textfield-component [wordstore]
  [:input {:type "text" 
           :value @nextword
           :class "form-control"
           :on-change #(reset! nextword (-> % .-target .-value))
           :on-key-down (keydownhandler wordstore @nextword)}])
