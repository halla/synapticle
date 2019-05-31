(ns app.player.grid
  (:require [app.player.player :as player]
            [app.player.multiplexer :as mux]
            [reagent.core :as reagent :refer [atom]]))

(def gridstate (atom [{ :channel_id 3 
                        :text "no-items"}]))
  
(defn render-item [item]
  [:div {:class "grid-item" :key (:channel_id item)} (:text item)])

(defn grid [_ _]
  (reify
    player/Player
    (step-fwd [this screen channels]
              (reset! gridstate (mux/get-item-from-each channels))
              (println @gridstate)
              screen)
    (step-rnd [this screen channels]
             (mux/get-item-from-each channels) 
              (reset! gridstate (mux/get-item-from-each channels))
              screen)
    (animation [this screen]
               screen)
    (render [this screen]            
            [:div {:class "grid"}
                        (map render-item @gridstate)])))