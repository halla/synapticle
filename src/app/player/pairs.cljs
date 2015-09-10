(ns app.player.pairs
  (:require [app.player.player :as player]
            [app.player.multiplexer :as mux]
            [reagent.core :as reagent :refer [atom]]))


(def pair (atom ["black" "white"])) ;; TODO move to db
(def index (atom 0)) ;; for ordered combinations, not implemented yet


(defn pairs [data presentation]
  (reify
    player/Player
    (step-fwd [_ screen channels] 
      (reset! pair (mux/get-combination channels))
      screen) 
    (step-rnd [_ screen channels] 
      (reset! pair (mux/get-combination channels))
      screen)
    (animation [_ screen]
      screen)
    (render [_ screen]
      [:div {:class "pairs"}
       [:div {:class "first panel"} (@pair 0)]
       [:div {:class "second panel"} (@pair 1)]])))
