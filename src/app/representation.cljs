(ns app.representation)


(defn fade-items [items]
  (map #(assoc % :opacity (- (:opacity %) 0.005)) items))


(defn fade-screen! [items]
  "Fade items, remove invisisible ones."
  (letfn [(update-items []
            (filter #(< 0 (:opacity %)) (fade-items @items)))]
    (reset! items (update-items))))

(defn fade-screen [items]
  "Fade items, remove invisisible ones."
  (letfn [(update-items []
            (filter #(< 0 (:opacity %)) (fade-items items)))]
    (update-items)))


(defn item->div [{:keys [text color x y size opacity key]} ]
  "Convert internal representation to hiccup html"
  [:div {:class "item" 
         :key key
         :style {:color color 
                 :opacity opacity
                 :left x 
                 :top y 
                 :font-size (str size "px")}
         
         } text ])
