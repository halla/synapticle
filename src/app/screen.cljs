(ns app.screen)

(def  colors [ "black", "darkblue", "darkred", "darkgreen", "darkolivegreen", "#555", "darkorange" ])

(defn collides? [div1 div2]
  ;; todo, replace naive guess with something less naive
  (and (< (Math/abs (- (:y div1) (:y div2))) 100)
       (< (Math/abs (- (:x div1) (:x div2))) 300)))



(defn divs-collision? [div2 divset]
  #_(> (count (filter #(collides? div2 %) @divs)) 0) ;; too much recursion..
  #_(println divset)
  #_(println (first divset))
  #_(collides? div2 (first divset))
  false
)


;; TODO maybe use right and bottom offsets to avoid crossing the window border
(defn max-width []
  (- (.-innerWidth js/window) 50))

(defn max-height []
  (-  (.-innerHeight js/window) 50))


(defn randomInt [min, max]
  (+ (rand-int (- max min)) min))


(defn gen-div-attrs []
  {:x (rand-int (max-width)) :y (rand-int (max-height)) :size (randomInt 12 40)
   :color (colors (rand-int (count colors)))})

#_(gen-div-attrs)

#_(defn gen-noncollide-attrs []
  (loop [d @divs] 
    (let [attrs (gen-div-attrs)]
      (if )
      )))

#_(defn gen-item [word]
  (let [attrs (some  #(when (not (divs-collision? % @divs)) %) [(gen-div-attrs)] #_(repeatedly gen-div-attrs))]
    (assoc attrs :text word :opacity 1.0 :key (str (rand-int 100000000))))) ; todo generate uuid for key



(defn gen-item [word]
  (let [attrs (gen-div-attrs)]
    (assoc attrs :text word :opacity 1.0 :key (str (rand-int 100000000))))) ; todo generate uuid for key

