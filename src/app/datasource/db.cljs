(ns app.datasource.db
  (:require [schema.core :as s :include-macros true]))

(def schema [{:title s/Str
              :items [s/Str]}])

(def default-value
  [{:title "Colors"
    :items ["red"
            "green"
            "blue"
            ]}  
   ])


