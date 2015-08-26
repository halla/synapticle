(ns app.players
  (:require [app.representation :as reps]
            [app.player :as player]
            [app.multiplexer :as mux]
            [app.screen :as screen]))

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
                     :size 20
                     :left "40%"
                     :top "30%"}} 
       (:text (first @screen) )])))
