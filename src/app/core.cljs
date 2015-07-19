(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [app.screen :as screen]
            [app.drizzle :as drizzle]
            [app.pairs :as pairs]
            [app.importer :as importer]
            [app.player :as player]
            [app.players :as players]
            [app.representation :as reps]
            [app.ctl :as ctl]
            [cljs.core.async :refer [chan mult tap <!]]
            [dragonmark.web.core :as dw :refer [xf xform]])
  (:use [domina.css :only [sel]]
        [domina.events :only [listen! target]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defn main []
  "Called on code reload"
  (println "RELOAD"))

(def eventbus-in (chan))
(def eventbus-out (mult eventbus-in))

(def config (atom { 
                   :n-concurrent-items 20
                   :items-per-sec 3.25
                   }))

(defonce print-timer (atom 0))
(defonce animation-timer (atom 0))

(defonce words 
;  "Data model"
  (atom ["ClojureScript" "In the browser" "Review your notes" "Generate random associations" "Preview text materials" "React" "Reagent"]))

(defonce divs 
 ; "Internal representation of the screen"
  (atom []))

(defonce controls-visible? (atom true))

(def playstates {:stopped "Stopped"
                 :running "Running"})
(defonce playstate (atom :stopped))

(def playmodes {"drizzle" (drizzle/drizzle words divs)
                "pairs" (pairs/pairs words divs)
                "single" (players/single words divs)})
(defonce playmode (atom "drizzle"))
(defonce randomize? (atom true))

(defn get-player []  
  (playmodes @playmode))

(defn mount-root []
  (reagent/render [ctl/control-panel eventbus-in
                   playstate
                   playstates
                   controls-visible?
                   playmode
                   config
                   randomize?
                   ] (.getElementById js/document "controls"))
  (reagent/render [player/render (get-player)] (.getElementById js/document "screen")))


(defn start []
  (let [interval-new (/ 1000 (:items-per-sec @config))
        interval-anim 50
        step-func (if @randomize? player/step-rnd player/step-fwd)]
    (println "STEP" @randomize?)
    (reset! print-timer  (js/setInterval #(step-func (get-player)) interval-new))
    (reset! animation-timer  (js/setInterval #(player/animation (get-player)) interval-anim))
    (reset! playstate :running)))

(defn stop []
  (js/clearInterval @print-timer )
  (js/clearInterval @animation-timer )
  (reset! playstate :stopped))

(defn restart []
  (println "RESTART" @randomize?)
  (mount-root)
  (stop)
  (start))

(let [eventbus (tap eventbus-out (chan))]
  (go (loop [] 
        (let [e (<! eventbus)]
          (println "EVENT" e)
          (cond 
            (= e :start) (start)
            (= e :stop) (stop)
            (= e :restart) (restart)
            (= e :textarea-import) (importer/textarea-import words))
          (recur)))))

(listen! (sel "#screen") :click (fn [evt] (reset! controls-visible? (not @controls-visible?))))
(restart)
