(ns app.representation
  (:require [re-frame.core :refer [dispatch-sync]]))


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


(defn item->div [{:keys [text color x y size opacity key] :as item} ]
  "Convert internal representation to hiccup html"
  [:div {:class "item-container"
         :key key
         :style {:left x
                 :top y
                 :overflow "hidden"}
         :on-mouse-over (fn [e]                         
                          (.addClass 
                           (.closest (js/jQuery (.. e -target)) 
                                     ".item-container") 
                           "active")
                          (dispatch-sync [:stop]))
         :on-mouse-out (fn [e] 
                         (.remove (.. e -target -classList) "active")
                         (dispatch-sync [:start]))}
   [:div {:class "item"           
          :style {:color color 
                  :opacity opacity
                  :font-size (str size "px")}         
          
          } text]
   [:div {:class "meta"
          :on-click (fn [e] 
                      (dispatch-sync [:delete text nil])
                      (dispatch-sync [:screen-rm text])
                      (dispatch-sync [:start]))} ;; mouse-out not triggered anymore
    "x"
    ]   
   ])
