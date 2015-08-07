(ns app.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [app.core-test]
            [app.misc-test]
            [app.screen-test]))

(enable-console-print!)

(defn ^:export run []
  (println "TEST RUNNER")
  (run-tests
   'app.core-test
   'app.misc-test
   'app.screen-test
))

;sometimes "not defined", sometimes works..
#_(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
    "Custom report end hook"
  (if (cljs.test/successful? m)
    (println "SUCCESS!")
    (println "FAIL")))
