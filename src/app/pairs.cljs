(ns app.pairs
  (:require [app.player :as player]               
            [reagent.core :as reagent :refer [atom]]))

(def items ["black" "white" "yellow" "green"])
(def pair (atom ["black" "white"]))
(def index (atom 0))

(defn get-combination [list] 
  (if (< (count list) 2)
    ["Add" "items"]
    (let [get-item #(list (rand-int (count list)))
          item1 (get-item)]
      (loop [trials 100]
        (let [item2 (get-item)]
          (if (= item1 item2)
            (recur (dec trials))
            [(get-item) (get-item)]))))))

(defn pairs [data presentation]
  (reify
    player/Player
    (step-fwd [_] (reset! pair (get-combination @data))) 
    (step-rnd [_] (reset! pair (get-combination @data)))
    (animation [_])
    (render [_]   
      [:div {:class "pairs"}
       [:div {:class "first panel"} (@pair 0)]
       [:div {:class "second panel"} (@pair 1)]
       ])))
