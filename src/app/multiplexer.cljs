(ns app.multiplexer)

(defn get-item [sources]
  (let [rnd-src (@sources (rand-int (count @sources)))
        items (:items rnd-src)
        rnd-item (items (rand-int (count items)))]
    rnd-item))
