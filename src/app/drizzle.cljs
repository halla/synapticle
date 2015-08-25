(ns app.drizzle
  (:require [app.player :as player]
            [app.screen :as screen]
            [app.multiplexer :as mux]
            [app.representation :as reps]))


(defn drizzle [data layout]
  (reify 
    player/Player
    (step-fwd [_ screen channels]
      #_(swap! layout #(conj % (screen/gen-item (mux/get-item data)))))
    (step-rnd [_ screen channels]
      (conj screen (screen/gen-item 
                    (mux/get-item channels) 
                    screen))
      #_(swap! layout #(conj % (screen/gen-item (mux/get-item data)))))
    (animation [_ screen] (reps/fade-screen screen))
    (render [_ screen]
      [:div {:key (str (rand-int 10000000))} (doall (for [i @screen] (reps/item->div i)))]
      )))

