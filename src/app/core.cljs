(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [app.screen :as screen]
            [app.drizzle :as drizzle]
            [app.pairs :as pairs]
            [app.importer :as importer]
            [app.player :as player]
            [app.players :as players]
            [app.datasource :as data]
            [app.representation :as reps]
            [app.ctl :as ctl]
            [cljsjs.jquery]
            [cljs.core.async :refer [chan mult tap <!]]
            [dragonmark.web.core :as dw :refer [xf xform]])
  (:use [domina.css :only [sel]]
        [domina.events :only [listen! target]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]))


(enable-console-print!)

(defn main []
  "Called on code reload"
  (reagent.core/flush)
  (println "RELOAD"))

(def eventbus-in (chan))
(def eventbus-out (mult eventbus-in))

(def config (atom { 
                   :n-concurrent-items 20
                   :items-per-sec 3.25
                   }))

(defonce print-timer (atom 0))
(defonce animation-timer (atom 0))

(def playstates {:stopped "Stopped"
                 :running "Running"})
(defonce playstate (atom :stopped))

(def playmodes {"drizzle" (drizzle/drizzle data/words screen/divs)
                "pairs" (pairs/pairs data/words screen/divs)
                "single" (players/single data/words screen/divs)})
(defonce playmode (atom "drizzle"))
(defonce randomize? (atom true))

(defn get-player []  
  (playmodes @playmode))


(defn mount-root []
  (reagent/render [ctl/control-panel eventbus-in
                   playstate
                   playstates                  
                   playmode
                   config
                   randomize?
                   data/wordlists
                   ] (.getElementById js/document "controls"))
  (reagent/render [importer/textfield-component data/words eventbus-in]
                  (.getElementById js/document "wordinputs"))
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

(defn clear-all []
  (data/clear)
  (screen/clear))

(defn restart []
  (println "RESTART" @randomize?)
  (screen/clear)
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
            (= e :clear) (clear-all)
            (= e :textarea-import) (importer/textarea-import data/words)
            (= e :data-updated) "do smtg"
            (and (map? e)
                 (contains? e :delete)) (data/delete (:delete e)))
          (recur)))))

(listen! (sel "#screen") :click 
         (fn [evt]
           (.toggle (js/jQuery "nav"))))

(restart)
