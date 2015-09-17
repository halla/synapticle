(ns app.datasource.db
  (:require [schema.core :as s :include-macros true]))


(def Tree {:value s/Str
           (s/optional-key :children) [(s/recursive #'Tree)]
           (s/optional-key :visible?) s/Bool})

(def schema [Tree]) ;; each "set" is a tree


(def default-value
  [{:value "Colors"
    :children [{:value "red"}
               {:value "green"}
               {:value "blue"}]}  
   {:value "Questions"
    :children [{:value "What if?"} 
               {:value "What else?"}]}
   {:value "Expand"
    :children [{:value "imagine"} 
               {:value "possible"} 
               {:value "future"}]}
   ])


