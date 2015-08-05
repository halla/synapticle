(ns app.multiplexer)

(defn get-item [sources]
  "Implicit gains for sources 1 1/2 1/3..., waiting for channels/generators"
  (let [gains (for [x (range 1  (inc (count [1 2 3])))] (/ 1 x))
        points (map #(* % (rand)) gains)
        winner-idx (first (apply max-key second (map-indexed vector points)))
        items (:items (@sources winner-idx))
        rnd-item (items (rand-int (count items)))]
    rnd-item))

(defn get-item-from-all [sources]
  "First concatenates, then picks."
  (let [items (vec (reduce #(concat %1 (:items %2)) [] @sources))
        rnd-item (items (rand-int (count items)))]
    rnd-item))
