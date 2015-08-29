(ns app.db
  (:require [reagent.core]
            [schema.core :as s :include-macros true]))

(def schema {:controls {:dataview-visible? s/Bool
                        :import-visible? s/Bool
                        :help-visible? s/Bool
                        :active-list-idx s/Int
                        :insert-mode? s/Bool}
             :player {:playstate (s/enum :running :stopped)
                      :playmode s/Str
                      :randomize? s/Bool
                      :print-timer s/Int
                      :animation-timer s/Int
                      :items-per-sec s/Num}
             :channels [{:title s/Str
                         :items [s/Str]
                         :gain s/Num
                         :muted? s/Bool}]
             :screen [{(s/optional-key :x) s/Int
                       (s/optional-key :y) s/Int
                       (s/optional-key :size) s/Int
                       (s/optional-key :color) s/Str
                       :text s/Str
                       (s/optional-key :opacity) s/Num
                       :key s/Str
                       }]})


(def defaults-player
  {:playstate :running
   :playmode "drizzle"
   :randomize? true
   :print-timer 0
   :animation-timer 0
   :items-per-sec 2.0 })

(def defaults-controls 
  {:dataview-visible? true
   :import-visible? false
   :help-visible? true
   :active-list-idx 0
   :insert-mode? false})

(def defaults-screen 
  [{:text "hep!"
    :size 20
    :color "green"
    :key "1234567" 
    :opacity 1.0
    :x 100
    :y 100}])

(def defaults-channels 
  [{:title "List 1"
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
    :items []
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
    :muted? false}])

(def default-value 
  {:controls defaults-controls
   :player defaults-player
   :channels defaults-channels
   :screen defaults-screen } )




(def lsk "db")

(defn ls->db
  []
  (some->> (.getItem js/localStorage lsk)
           (cljs.reader/read-string)))

(defn db->ls!
  [db]
  (.setItem js/localStorage lsk (str db)))
