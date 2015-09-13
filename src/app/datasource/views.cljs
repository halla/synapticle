(ns app.datasource.views)


(def dummylist ["Set 1" "Set 2"])
(defn dataset-list []
  [:ul  (for [item dummylist] [:li item]) ])


(defn browser []
  "Browse available data sources"
  (dataset-list)
  )


(defn editor []
  "Edit data"
  )

