(ns app.handlers
  (:require [re-frame.core :refer [register-handler dispatch-sync after path]]
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


(def ->ls (after db/cfg->ls!))

(def cfg-mw [->ls])
(def channel-mw [(path :channels)])

(def playstates {:stopped "Stopped"
                 :running "Running"})

(def playmodes {"drizzle" drizzle/drizzle
                "pairs" pairs/pairs
                "single" players/single})

(defn get-player [mode channels]
  ((playmodes mode) channels screen/divs))

;: -- re-frame style handlers

(register-handler 
 :initialize-db 
 (fn [_ _] (merge db/default-value (db/ls->cfg))))

(register-handler 
 :toggle-dataview-visibility 
 (fn [db _] (update-in db [:dataview-visible?] #(not %))))

(register-handler 
 :toggle-import-visibility 
 (fn [db _] (update-in db [:import-visible?] #(not %))))


(defn start [db [_]]
  (let [interval-new (/ 1000 (:items-per-sec db))
        interval-anim 50
        step-func (if (:randomize? db) player/step-rnd player/step-fwd)]
    (js/clearInterval (:print-timer db))
    (js/clearInterval (:animation-timer db))
    (merge db {:print-timer (js/setInterval #(step-func (get-player (:playmode db) (:channels db))) interval-new)
               :animation-timer (js/setInterval #(player/animation (get-player (:playmode db) (:channels db))) interval-anim)
               :playstate :running})))

(register-handler :start start)

(defn stop [db [_]]
  (js/clearInterval (:print-timer db))
  (js/clearInterval (:animation-timer db))
  (assoc-in db [:playstate] :stopped))

(register-handler :stop stop)

(register-handler 
 :toggle-play 
 (fn [db [_]] 
   (if (= (:playstate db) :stopped)
     (start db [])
     (stop db []))))

(register-handler
 :set-randomize
 (fn [db [_ randomize?]]
   (assoc-in db [:randomize?] randomize?)))

(register-handler
 :set-playmode
 cfg-mw
 (fn [db [_ playmode]]
   (assoc-in db [:playmode] playmode)))

(register-handler
 :set-ipm
 cfg-mw
 (fn [db [_ ipm]]
   (assoc-in db [:items-per-sec] (/ ipm 60))))

(register-handler 
 :set-active-channel 
 (fn [db [_ idx]]  (assoc-in db [:active-list-idx] idx) ))

;; --- Channel related

(defn delete [vect item]
  (vec (remove (fn [word] (= word item)) vect)))

(register-handler 
 :delete
 channel-mw
 (fn [channels [_ item channel]] 
   (mapv #(if (= % channel)
            (assoc % :items (delete (:items %) item)) 
            %) channels)))


(register-handler 
 :channel-set-mix
 [cfg-mw
  channel-mw]
 (fn 
   [channels [_ channel value]] 
   (vec (map #(if (= % channel)
                (assoc % :gain value) 
                %) channels))))

;; todo decouple this
(register-handler 
 :clear 
 channel-mw
 (fn [channels [_ channel]]
   (screen/clear)
   (vec (map #(if (= % channel)
                (assoc % :items [])
                %) channels))))

(register-handler
 :mute
 channel-mw
 (fn [channels [_ channel]]
   (vec (map #(if (= % channel)
                (assoc % :muted? (not (:muted? %))) 
                %) channels))))


(register-handler
 :import
 channel-mw
 (fn [channels [_ text channel]]
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) (importer/process-input text)))) 
                %) channels))))

(register-handler
 :words-add
 channel-mw
 (fn [channels [_ words channel]]
   channels
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) words))) 
                %) channels))))
