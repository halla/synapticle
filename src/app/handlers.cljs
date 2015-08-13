(ns app.handlers
  (:require [re-frame.core :refer [register-handler]]
            [cljs.core.async :refer [put!]]
            [app.db :as db]
            [app.screen :as screen]
            [app.datasource :as data]
            [app.ctl :as ctl]))

;: -- re-frame style handlers

(register-handler 
 :initialize-db 
 (fn [_ _] db/default-value))

(register-handler 
 :toggle-dataview-visibility 
 (fn [db _] (update-in db [:dataview-visible?] #(not %))))

;; todo decouple this
(register-handler 
 :clear 
 (fn [db _] 
   (data/clear (@data/wordlists (:active-list-idx db)))
   (screen/clear)
   db))

(register-handler 
 :set-active-channel 
 (fn [db [_ idx]]  (assoc-in db [:active-list-idx] idx) ))

;todo remove eventbus dep
(defn start [eventbus-in]
  (put! eventbus-in :start))

(defn stop [eventbus-in]
  (put! eventbus-in :stop))

(defn toggleplay [playstate eventbus-in]
  (if (= @playstate :stopped)
    (start eventbus-in)
    (stop eventbus-in)))

(register-handler 
 :toggle-play 
 (fn [db [_ playstate eventbus-in]] (toggleplay playstate eventbus-in) db))

(register-handler 
 :channel-set-mix
 (fn 
   [db [_ data value]] 
   (data/set-mix! 
    (@data (:active-list-idx db)) 
    value)
   db))

(register-handler 
 :delete 
 (fn [db [_ item]] 
   (data/delete! (:active-list-idx db) item)
   db))

(register-handler
 :set-ipm
 (fn [db [_ ipm]]
   (assoc-in db [:items-per-sec] ipm)))
