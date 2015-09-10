(ns app.player.handlers
  (:require [re-frame.core :refer [trim-v after register-handler path
                                   dispatch-sync]]
            [app.db :as db]
            [app.player.player :as player]
            [schema.core :as s :include-macros true]))


(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))


(def check-schema-mw (after (partial check-and-throw db/schema))) ;; todo share with main handler

(def player-mw [check-schema-mw
                (path :player)
                trim-v])


;; -- Player

(defn start [db [_]] ;; TODO remove channel dep
  (let [player (:player db)
        interval-new (/ 1000 (:items-per-sec player))
        interval-anim 50
        step-func (if (:randomize? player) player/step-rnd player/step-fwd)]
    (js/clearInterval (:print-timer player))
    (js/clearInterval (:animation-timer player))
    (merge-with merge db 
                {:player {:print-timer (js/setInterval 
                                        #(dispatch-sync [:step])
                                        interval-new)
                          :animation-timer (js/setInterval 
                                            #(dispatch-sync [:animate])
                                            interval-anim)
                          :playstate :running}})))

(register-handler 
 :start 
 [trim-v]
 start)

(defn stop [db [_]]
  (js/clearInterval (get-in db [:player :print-timer]))
  (js/clearInterval (get-in db [:player :animation-timer]))
  (assoc-in db [:player :playstate] :stopped))

(register-handler 
 :stop 
 [trim-v]
 stop)

(register-handler 
 :toggle-play 
 [trim-v]
 (fn [db [_]] 
   (if (= (get-in db [:player :playstate]) :stopped)
     (start db [])
     (stop db []))))


(register-handler
 :set-randomize
 player-mw
 (fn [db [randomize?]]
   (assoc-in db [:randomize?] randomize?)))


(register-handler
 :set-playmode
 [player-mw]
 (fn [db [playmode]]
   (assoc-in db [:playmode] playmode)))

(register-handler
 :set-ipm
 player-mw
 (fn [db [ipm]]
   (assoc-in db [:items-per-sec] (/ ipm 60))))
