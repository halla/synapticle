(ns app.views
  (:require [app.player.player :as player]
            [app.player.handlers :as playerhandlers]
            [re-frame.core :refer [subscribe]])
  (:require-macros [reagent.ratom :refer [reaction]]))


(defn render-player []
  (let [player (subscribe [:player])
        playmode (reaction (:playmode @player))
        channels (subscribe [:channels])
        plr (reaction (playerhandlers/get-player @playmode @channels))
        screen (subscribe [:screen])]
    #(player/render @plr screen)))

