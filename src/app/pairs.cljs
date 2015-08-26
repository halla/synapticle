(ns app.pairs
  (:require [app.player :as player]
            [app.multiplexer :as mux]
            [reagent.core :as reagent :refer [atom]]))

(def items ["black" "white" "yellow" "green"])
(def pair (atom ["black" "white"]))
(def index (atom 0))


(defn get-combination [sources]
  (if false ;; TODO check empty sources
    ["Add" "items"]
    (let [item1 (mux/get-item sources)]
      (loop [trials 100]
        (let [item2 (mux/get-item sources)]
          (if (= item1 item2)
            (recur (dec trials))
            [item1 item2]))))))

(defn pairs [data presentation]
  (reify
    player/Player
    (step-fwd [_ screen channels] 
      (reset! pair (get-combination channels))
      screen) 
    (step-rnd [_ screen channels] 
      (reset! pair (get-combination channels))
      screen)
    (animation [_ screen]
      screen)
    (render [_ screen]
      [:div {:class "pairs"}
       [:div {:class "first panel"} (@pair 0)]
       [:div {:class "second panel"} (@pair 1)]
       ])))
