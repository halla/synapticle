(ns app.views
  (:require [app.player :as player]
            [app.handlers :as handlers]
            [re-frame.core :refer [subscribe]]))


(defn render-player []
  (let [playmode (subscribe [:playmode])
        channels (subscribe [:channels])]   
    #(player/render (handlers/get-player @playmode @channels))))
