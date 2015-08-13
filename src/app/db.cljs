(ns app.db
  (:require [reagent.core]))


(def default-value 
  {:playstate :stopped
   :playmode "drizzle"
   :randomize? true
   :print-timer 0
   :animation-timer 0
   :dataview-visible? true
   :active-list-idx 0
   :items-per-sec 2.0})
