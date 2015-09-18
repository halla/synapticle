(ns app.player.drizzle
  (:require [app.player.player :as player]
            [app.player.screen :as screen]
            [app.player.multiplexer :as mux]
            [app.player.representation :as reps]))

(def frame (atom 0))

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
    (animation [_ screen] 
      (swap! frame #(inc %)) 
      (if (= (mod @frame 2) 0) ;; 25/s 
        (reps/fade-screen screen 0.02)
        screen))
    (render [_ screen]
      [:div {:key (str (rand-int 10000000))} (doall (for [i @screen] (reps/item->div i)))]
      )))

