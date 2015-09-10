(ns app.player.players
  (:require [app.player.representation :as reps]
            [app.player.player :as player]
            [app.player.multiplexer :as mux]
            [app.player.screen :as screen]))

(defonce index (atom 0))

(defn single [data layout]
  (reify 
    player/Player
    (step-fwd [_ screen channels]
      (let [item  [{:text ((:items (@data 0)) (mod @index (count @data)))
                    :key (str (rand-int 100000000))
                    :opacity 1.0}]]
        (swap! index inc)
        item))
    (step-rnd [_ screen channels]
      [{:text (mux/get-item channels) 
        :key (str (rand-int 100000000))
        :opacity 1.0}])
    (animation [_ screen]
      screen)
    (render [_ screen]
      [:div {:class "single" 
             :key (rand-int 1000000)
             :style {:position "absolute"
                     :opacity 1.0
                     :font-size "28px"
                     :text-align "center"
                     :width "90%";
                     :top "30%"}} 
       (:text (first @screen) )])))
