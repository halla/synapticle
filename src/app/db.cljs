(ns app.db
  (:require [reagent.core]))


(def default-value 
  {:playstate :running
   :playmode "drizzle"
   :randomize? true
   :print-timer 0
   :animation-timer 0
   :dataview-visible? true
   :import-visible? false
   :active-list-idx 0
   :items-per-sec 2.0})




(def lsk "synapticle")

(defn ls->cfg 
  []
  (some->> (.getItem js/localStorage lsk)
           (cljs.reader/read-string)))

(defn cfg->ls!
  [cfg]
  (.setItem js/localStorage lsk (str cfg)))
