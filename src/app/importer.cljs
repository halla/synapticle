(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]
            [app.datasource :as data]
            [app.ctl :as ctl]
            [clojure.string :as str]
            [cljs.core.async :refer [put!]]))


(defn tokenize-content []
  (concat (vec (for [page js/content]                 
                 (.split page ". ")
                 ))))


(defn split-by-line [input]
  (let [in (if (sequential? input) input [input])] 
    (flatten (map #(str/split  % #"\n") in))))

(defn trim-items [input]
  (map #(clojure.string/trim %) input))

(defn remove-empty [input] ;; comp throws an error..
  (filter #(not= "" %) input) )

(defn process-input [input]
  (vec (distinct (remove-empty (trim-items (split-by-line input))))))

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
