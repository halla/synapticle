(ns views.cljs
  (:require [app.player :as player]
            [app.handlers :as handlers]))


(defn render-player []
  (let [playmode (subscribe [:playmode])]   
    #(player/render (handlers/get-player @playmode))))
