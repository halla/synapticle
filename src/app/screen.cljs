(ns app.screen
  (:require
   [reagent.core :as reagent :refer [atom]]))

(defonce divs 
 ; "Internal representation of the screen"
  (atom []))

(def  colors [ "black", "darkblue", "darkred", "darkgreen", "darkolivegreen", "#555", "darkorange" ])


(defn clear []
  (reset! divs []))

(defn collides? [div1 div2]
  ;; todo, replace naive guess with something less naive
  (or (< (Math/abs (- (:y div1) (:y div2))) 100)
      (< (Math/abs (- (:x div1) (:x div2))) 300)))


(defn divs-collision? [div2 divset]  
  (if (empty? divset)
    false
    (if (collides? div2 (first divset))
      true
      (recur div2 (rest divset)))))


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


(defn gen-noncollide-attrs []
  (loop [trials 100] 
    (let [div2 (gen-div-attrs)]      
      (if (and (divs-collision? div2 @divs)
               (> trials 0))
        (recur (dec trials))
        div2))))


(defn gen-item [word]
  (let [attrs (gen-noncollide-attrs)]
    (assoc attrs :text word :opacity 1.0 :key (str (rand-int 100000000))))) ; todo generate uuid for key

