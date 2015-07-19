(ns app.templates
  (:require [clojure.java.io :refer (resource)]))

;; as in stackoverflow.com/questions/23767729/clojurescript-templating-from-html-files

(defmacro deftmpl
  "Read template from file in resources/"
  [symbol-name html-name]
  (let [content (slurp (resource html-name))]
    `(def ~symbol-name
       ~content)))
