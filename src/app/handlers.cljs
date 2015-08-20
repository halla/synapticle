(ns app.handlers
  (:require [re-frame.core :refer [register-handler 
                                   dispatch-sync after path
                                   trim-v debug]]
            [cljs.core.async :refer [put!]]
            [app.db :as db]
            [app.screen :as screen]
            [app.player :as player]
            [app.importer :as importer]
            [app.datasource :as data]
            [app.drizzle :as drizzle]
            [app.pairs :as pairs]
            [app.players :as players]  
            [app.ctl :as ctl]))


(def ->ls (after db/db->ls!))

(def player-mw [trim-v])
(def controls-mw [(path :controls)
                  trim-v])

(def channel-mw [->ls
                 (path :channels)                 
                 trim-v])

(def playstates {:stopped "Stopped"
                 :running "Running"})

(def playmodes {"drizzle" drizzle/drizzle
                "pairs" pairs/pairs
                "single" players/single})

(defn get-player [mode channels] ; runs on every animation frame?  
  ((playmodes mode) channels screen/divs))

;: -- re-frame style handlers

(register-handler 
 :initialize-db 
 [debug]
 (fn [_ _] (merge db/default-value (db/ls->db))))


;; -- Player

(defn start [db [_]]
  (let [player (:player db)
        interval-new (/ 1000 (:items-per-sec player))
        interval-anim 50
        step-func (if (:randomize? player) player/step-rnd player/step-fwd)]
    (js/clearInterval (:print-timer player))
    (js/clearInterval (:animation-timer player))
    (merge-with merge db {:player {:print-timer (js/setInterval 
                                                  #(step-func (get-player (:playmode player) (:channels db))) interval-new)
                                    :animation-timer (js/setInterval 
                                                      #(player/animation 
                                                        (get-player (:playmode player) (:channels db))) interval-anim)
                                    :playstate :running}})))



(register-handler 
 :start 
 [player-mw]
 start)

(defn stop [db [_]]
  (js/clearInterval (get-in  db [:player :print-timer]))
  (js/clearInterval (get-in db [:player :animation-timer]))
  (assoc-in db [:player :playstate] :stopped))

(register-handler 
 :stop 
 player-mw
 stop)

(register-handler 
 :toggle-play 
 player-mw
 (fn [db [_]] 
   (if (= (:playstate db) :stopped)
     (start db [])
     (stop db []))))

(register-handler
 :set-randomize
 player-mw
 (fn [db [randomize?]]
   (assoc-in db [:randomize?] randomize?)))


(register-handler
 :set-playmode
 player-mw
 (fn [db [playmode]]
   (assoc-in db [:playmode] playmode)))

(register-handler
 :set-ipm
 player-mw
 (fn [db [ipm]]
   (assoc-in db [:items-per-sec] (/ ipm 60))))


;; ----- Controls 

(register-handler 
 :toggle-dataview-visibility 
 controls-mw
 (fn [db _] (update-in db [:dataview-visible?] #(not %))))

(register-handler 
 :toggle-import-visibility 
 controls-mw
 (fn [db _] (update-in db [:import-visible?] #(not %))))

(register-handler 
 :set-active-channel 
 controls-mw
 (fn [db [idx]]  (assoc-in db [:active-list-idx] idx) ))


;; --- Channel related

(defn delete [vect item]
  (vec (remove (fn [word] (= word item)) vect)))

(register-handler 
 :delete
 channel-mw
 (fn [channels [item channel]] 
   (mapv #(if (= % channel)
            (assoc % :items (delete (:items %) item)) 
            %) channels)))


(register-handler 
 :channel-set-mix
 channel-mw
 (fn 
   [channels [channel value]] 
   (vec (map #(if (= % channel)
                (assoc % :gain value) 
                %) channels))))

;; todo decouple this
(register-handler 
 :clear 
 channel-mw
 (fn [channels [channel]]
   (screen/clear)
   (vec (map #(if (= % channel)
                (assoc % :items [])
                %) channels))))

(register-handler
 :mute
 channel-mw
 (fn [channels [channel]]
   (vec (map #(if (= % channel)
                (assoc % :muted? (not (:muted? %))) 
                %) channels))))


(register-handler
 :import
 channel-mw
 (fn [channels [text channel]]
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) (importer/process-input text)))) 
                %) channels))))

(register-handler
 :words-add
 channel-mw
 (fn [channels [words channel]]
   channels   
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) words))) 
                %) channels))))
