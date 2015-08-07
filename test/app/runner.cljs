(ns app.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [app.misc-test]
            [app.screen-test]))

(enable-console-print!)

(defn ^:export run []
  (println "TEST RUNNER")
  (run-tests
   'app.misc-test
   'app.screen-test))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "SUCCESS!")
    (println "FAIL")))
