(ns app.importer
  (:require [reagent.core :as reagent :refer [atom]]
            [app.datasource :as data]
            [app.ctl :as ctl]
            [cljs.core.async :refer [put!]]))


(defn tokenize-content []
  (concat (vec (for [page js/content]                 
                 (.split page ". ")
                 ))))


(defn textarea-import! [words eventbus-in]
  (let [ws (js->clj (.split (.-value (.getElementById js/document "textareaimport")) "\n"))
        ws2 (vec (distinct (filter #(not= "" %) ;; comp throws an error..
                                   (map #(clojure.string/trim %) 
                                        ws))))]
    (data/add-multiple! (@data/wordlists @ctl/active-list-idx) ws2)
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
