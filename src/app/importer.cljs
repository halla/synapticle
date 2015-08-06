(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]
            [app.datasource :as data]
            [app.ctl :as ctl]
            [clojure.string :as str]
            [cljs.core.async :refer [put!]]))


(def tokenize-content (concat (vec (for [page js/content]                 
                 (.split page ". ")
                 ))))

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
        ws2 (process-input ws)]
    (data/add-multiple! (@data/wordlists @ctl/active-list-idx) ws2)
    (aset (.getElementById js/document "textareaimport") "value" "")
    (put! eventbus-in :data-updated)))

;; single word input field

(defonce nextword (atom ""))

(defn textfield-import! [words word eventbus-in]
  (data/add! (@data/wordlists @ctl/active-list-idx) (js->clj word))
  (reset! nextword "")
  (put! eventbus-in :data-updated))


(defn keydownhandler [wordstore word eventbus-in]
  (fn [e]
    (when (= (.-which e) 27) ;esc
      (reset! nextword ""))
    (when (= (.-key e) "Enter")
      (textfield-import! wordstore word eventbus-in))))

(defn textfield-component [wordstore eventbus-in]
  [:input {:type "text" 
           :value @nextword
           :placeholder (str "Add item to " (:title (@data/wordlists @ctl/active-list-idx)))
           :class "form-control"
           :on-change #(reset! nextword (-> % .-target .-value))
           :on-key-down (keydownhandler wordstore @nextword eventbus-in)}])
