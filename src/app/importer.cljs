(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe]]
            [app.datasource :as data]
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


(defn textarea-import! [words eventbus-in]
  (let [ws (js->clj (.-value (.getElementById js/document "textareaimport")))
        ws2 (process-input ws)
        active-list-idx (subscribe [:active-list-idx])]
    (data/add-multiple! (@data/wordlists @active-list-idx) ws2)
    (aset (.getElementById js/document "textareaimport") "value" "")
    (put! eventbus-in :data-updated)))

;; single word input field

(defonce nextword (atom ""))

(defn textfield-import! [words word eventbus-in active-list-idx]
  (data/add-multiple! (@data/wordlists @active-list-idx) (process-input (js->clj word)))
  (reset! nextword "")
  (put! eventbus-in :data-updated))


(defn keydownhandler [wordstore word eventbus-in active-list-idx]
  (fn [e]
    (when (= (.-which e) 27) ;esc
      (reset! nextword ""))
    (when (= (.-key e) "Enter")
      (textfield-import! wordstore word eventbus-in active-list-idx))))

(defn textfield-component [wordstore eventbus-in]
  (let [active-list-idx (subscribe [:active-list-idx])]
    [:input {:type "text" 
             :value @nextword
             :placeholder (str "Add item to " (:title (@data/wordlists @active-list-idx)))
             :class "form-control"
             :on-change #(reset! nextword (-> % .-target .-value))
             :on-key-down (keydownhandler wordstore @nextword eventbus-in active-list-idx)}]))
