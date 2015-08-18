(ns app.handlers
  (:require [re-frame.core :refer [register-handler dispatch-sync after]]
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

(def playstates {:stopped "Stopped"
                 :running "Running"})

(def playmodes {"drizzle" (drizzle/drizzle data/wordlists screen/divs)
                "pairs" (pairs/pairs data/wordlists screen/divs)
                "single" (players/single data/wordlists screen/divs)})

(defn get-player [mode]
  (playmodes mode))

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
    (merge db {:print-timer (js/setInterval #(step-func (get-player (:playmode db))) interval-new)
               :animation-timer (js/setInterval #(player/animation (get-player (:playmode db))) interval-anim)
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

(register-handler 
 :delete
 cfg-mw
 (fn [db [_ item]] 
   (data/delete! (:active-list-idx db) item)
   db))


(register-handler
 :textarea-import
 cfg-mw
 (fn [db [_]]
   (importer/textarea-import! data/words)
   db))

(register-handler 
 :channel-set-mix
 cfg-mw
 (fn 
   [db [_ data value]] 
   (data/set-mix! 
    (@data (:active-list-idx db)) 
    value)
   db))

;; todo decouple this
(register-handler 
 :clear 
 (fn [db _] 
   (data/clear (@data/wordlists (:active-list-idx db)))
   (screen/clear)
   db))

(register-handler
 :mute
 (fn [db [_ channel]]
   (data/toggle-muted channel)))
