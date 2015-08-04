(ns app.drizzle
  (:require [app.player :as player]
            [app.screen :as screen]
            [app.representation :as reps]))

(defn get-item [sources]
  (println "GET" sources)
  (let [rnd-src (@sources (rand-int (count @sources)))
        items (:items rnd-src)
        rnd-item (items (rand-int (count items)))]
    rnd-item))


(defn drizzle [data layout]
  (reify 
    player/Player
    (step-fwd [_]
      (swap! layout #(conj % (screen/gen-item (get-item data)))))
    (step-rnd [_]
      (swap! layout #(conj % (screen/gen-item (get-item data)))))
    (animation [_] (reps/fade-screen! layout))
    (render [_]
      [:div {:key (str (rand-int 10000000))} (doall (for [i @layout] (reps/item->div i)))]
      )))
