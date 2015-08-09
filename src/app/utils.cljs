(ns app.utils)

(defn in? 
  "Does seq contain element?"
  [seq elem]  
  (if  (some #(= elem %) seq)
    true
    false))
