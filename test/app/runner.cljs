(ns app.runner
  (:require [cljs.test :refer-macros [run-tests]]))

(enable-console-print!)

(defn ^:export run []
  (println "TEST RUNNER")
  (run-tests 'app.misc-test))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))
