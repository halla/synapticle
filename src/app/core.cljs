(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch-sync subscribe]]
            [app.screen :as screen]
            [app.drizzle :as drizzle]
            [app.pairs :as pairs]
            [app.importer :as importer]
            [app.player :as player]
            [app.players :as players]
            [app.datasource :as data]
            [app.representation :as reps]
            [app.ctl :as ctl]
            [app.handlers]
            [app.subscribables]
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

(defonce print-timer (atom 0))
(defonce animation-timer (atom 0))

(def playstates {:stopped "Stopped"
                 :running "Running"})
(defonce playstate (atom :stopped))

(def playmodes {"drizzle" (drizzle/drizzle data/wordlists screen/divs)
                "pairs" (pairs/pairs data/wordlists screen/divs)
                "single" (players/single data/wordlists screen/divs)})
(defonce playmode (atom "drizzle"))
(defonce randomize? (atom true))

(defn get-player []  
  (playmodes @playmode))


(defn mount-root []
  (reagent/render [ctl/control-panel eventbus-in
                   playstate
                   playstates                  
                   playmode                   
                   randomize?
                   data/wordlists
                   ] (.getElementById js/document "controls"))
  (reagent/render [importer/textfield-component data/wordlists eventbus-in]
                  (.getElementById js/document "wordinputs"))
  (reagent/render [player/render (get-player)] (.getElementById js/document "screen")))

(let [items-per-sec (subscribe [:items-per-sec])]
  (defn start []
    (let [interval-new (/ 1000 @items-per-sec)
          interval-anim 50
          step-func (if @randomize? player/step-rnd player/step-fwd)]
      (reset! print-timer  (js/setInterval #(step-func (get-player)) interval-new))
      (reset! animation-timer  (js/setInterval #(player/animation (get-player)) interval-anim))
      (reset! playstate :running))))

(defn stop []
  (js/clearInterval @print-timer )
  (js/clearInterval @animation-timer )
  (reset! playstate :stopped))


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
            (= e :textarea-import) (importer/textarea-import! data/words eventbus-in)
            (= e :data-updated) "do smtg"
            )
          (recur)))))

(listen! (sel "#screen") :click 
         (fn [evt]
           (.toggle (js/jQuery "nav"))))

(dispatch-sync [:initialize-db])
(restart)
