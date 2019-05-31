(ns app.player.multiplexer)
(enable-console-print!)

(defn get-next-source [sources]
  "Probability of selection based on source mix-level"
  (let [active-sources (filter #(not (or (:muted? %) 
                                         (= 0 (count (:items %))))) sources)
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
  "Concatenate all sources to a single set. Pick a random item."
  (let [items (vec (reduce #(concat %1 (:items %2)) [] sources))
        rnd-item (items (rand-int (count items)))]
    rnd-item))

(defn get-combination [sources]
  (if false ;; TODO check empty sources
    ["Add" "items"]
    (let [item1 (get-item sources)]
      (loop [trials 100]
        (let [item2 (get-item sources)]
          (if (= item1 item2)
            (recur (dec trials))
            [item1 item2]))))))

(defn get-item-from-each [sources]  
  (let [active-sources (filter #(not (or (:muted? %)
                                         (= 0 (count (:items %))))) sources)]
    (map (fn [x] {:channel_id (:id x)
                  :text (rand-nth (:items x))}) active-sources)))


    