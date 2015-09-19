(ns app.datasource.views
  (:require [re-frame.core :refer [dispatch-sync
                                   subscribe
                                   ]]))


(def dummylist ["Set 1" "Set 2"])


(defn allow-drop [e]
  (.preventDefault e)) 

(declare dataset-list-item)

(defn dataset-list-root [root]  
  (enable-console-print!)
  (println root)
  [:ul (dataset-list-item root)])


(defn dataset-list-item [{:keys [value children] :as root}]
  (list
   [:li {:draggable true
         :on-drag-start #(.setData (.-dataTransfer %) "text" value)
         } value]
   (when children
     (for [child children]
       (dataset-list-root child)))))


(defn dataset-list [datasets]
  [:div {:class "inner"}
   [:h3 "Data browser"]  
   (for [dataset datasets] (dataset-list-root dataset)) ])


(defn browser []
  (let [datasets (subscribe [:datasets])]
    "Browse available data sources"
    (dataset-list @datasets)))


(defn editor []
  "Edit data"
  )

