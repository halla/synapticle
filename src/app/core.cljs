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
            [app.handlers :as handlers]
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


(defn mount-root []
  (reagent/render [ctl/control-panel eventbus-in
                   handlers/playstates                  
                   data/wordlists
                   ] (.getElementById js/document "controls"))
  (reagent/render [importer/textfield-component data/wordlists eventbus-in]
                  (.getElementById js/document "wordinputs"))
  (let [playmode (subscribe [:playmode])]
    (reagent/render [player/render (handlers/get-player @playmode)] (.getElementById js/document "screen"))))


(defn restart []
  (screen/clear)
  (mount-root)
  (dispatch-sync [:stop])
  (dispatch-sync [:start]))

(let [eventbus (tap eventbus-out (chan))]
  (go (loop [] 
        (let [e (<! eventbus)]
          (println "EVENT" e)
          (cond 
            (= e :restart) (restart)
            (= e :textarea-import) (importer/textarea-import! data/words eventbus-in)
            )
          (recur)))))

(listen! (sel "#screen") :click 
         (fn [evt]
           (.toggle (js/jQuery "nav"))))

(dispatch-sync [:initialize-db])
(restart)
