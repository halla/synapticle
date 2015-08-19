(ns app.db
  (:require [reagent.core]))


(def default-value 

  {
   :playstate :running
   :playmode "drizzle"
   :randomize? true
   :print-timer 0
   :animation-timer 0
   :dataview-visible? true
   :import-visible? false
   :active-list-idx 0
   :items-per-sec 2.0
   :channels [{:title "List 1"
          :items ["Collide ideas"
                  "Review your notes" 
                  "Generate random associations" 
                  "Preview text materials" 
                  "Computer assisted thinking" 
                  "Immerse yourself on a subject"
                  "Just relax and watch the screen"
                  "Let your thoughts wander"                  
                  "If you get an idea, add it to the wordlist"
                  "You can pause the screen to hold a thought"]
          :gain 0.7
          :muted? false}
         {:title "List 2"
          :items ["hep"]
          :gain 0.7
          :muted? false}
         {:title "List 3"
          :items []
          :gain 0.7
          :muted? false}
         {:title "List 4"
          :items []
          :gain 0.7
          :muted? false}
         {:title "Expand"
          :items ["imagine" 
                  "possible"
                  "future"]
          :gain 0.3
          :muted? false}
         {:title "Questions"
          :items ["What if?" "What else?"]
          :gain 0.2
          :muted? false}]})




(def lsk "synapticle")

(defn ls->cfg 
  []
  (some->> (.getItem js/localStorage lsk)
           (cljs.reader/read-string)))

(defn cfg->ls!
  [cfg]
  (.setItem js/localStorage lsk (str cfg)))
