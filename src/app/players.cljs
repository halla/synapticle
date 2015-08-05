(ns app.players
  (:require [app.representation :as reps]
            [app.player :as player]
            [app.multiplexer :as mux]
            [app.screen :as screen]))

(defonce index (atom 0))

(defn single [data layout]
  (reify 
    player/Player
    (step-fwd [_]
      (reset! layout {:text ((:items (@data 0)) (mod @index (count @data)))
                      :opacity 1.0})
      (swap! index inc))
    (step-rnd [_]
      (reset! layout  {:text (mux/get-item data) 
                       :opacity 1.0}))
    (animation [_])
    (render [_]
      [:div {:class "single" 
             :key (rand-int 1000000)
             :style {:position "absolute"
                     :opacity 1.0
                     :left "40%"
                     :top "30%"}} 
       (:text @layout)])))
