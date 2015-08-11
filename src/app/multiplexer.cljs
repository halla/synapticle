(ns app.multiplexer)

(defn get-next-source [sources]
  "Probability of selection based on source mix-level"
  (let [active-sources (filter #(not (or (:muted? %) 
                                         (= 0 (count (:items %))))) @sources)
        gains (for [x active-sources] (:gain x))
        points (map #(* % (rand)) gains)
        winner-idx (first (apply max-key second (map-indexed vector points)))]
    ((vec active-sources) winner-idx)))


(defn get-item [sources]
  (let [source (get-next-source sources)
        items (:items source)
        rnd-item (items (rand-int (count items)))]
    rnd-item))

(defn get-item-from-all [sources]
  "First concatenates, then picks."
  (let [items (vec (reduce #(concat %1 (:items %2)) [] @sources))
        rnd-item (items (rand-int (count items)))]
    rnd-item))

