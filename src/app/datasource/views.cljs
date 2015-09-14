(ns app.datasource.views
  (:require [re-frame.core :refer [dispatch-sync
                                   subscribe
                                   ]]))


(def dummylist ["Set 1" "Set 2"])

(defn dataset-list [datasets]
  [:ul  (for [dataset datasets] [:li (:title dataset)]) ])


(defn browser []
  (let [datasets (subscribe [:datasets])]
    "Browse available data sources"
    (dataset-list @datasets))
  )


(defn editor []
  "Edit data"
  )

