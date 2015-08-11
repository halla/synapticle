(ns app.drizzle
  (:require [app.player :as player]
            [app.screen :as screen]
            [app.multiplexer :as mux]
            [app.representation :as reps]))


(defn drizzle [data layout]
  (reify 
    player/Player
    (step-fwd [_]
      (swap! layout #(conj % (screen/gen-item (mux/get-item data)))))
    (step-rnd [_]
      (swap! layout #(conj % (screen/gen-item (mux/get-item data)))))
    (animation [_] (reps/fade-screen! layout))
    (render [_]
      [:div {:key (str (rand-int 10000000))} (doall (for [i @layout] (reps/item->div i)))]
      )))
