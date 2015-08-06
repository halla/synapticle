(ns app.misc-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [app.importer :as importer]))


(deftest test-split-by-line
  (let [input ["hey hey\nyou\nthere"]
        output (sequence (comp importer/split-by-line) input)
        ]
    (is (= ["hey hey" "you" "there"] output))))


(deftest test-process-input
  (let [input " hey hey\n  you  \nthere "
        output (importer/process-input input)]
    (println "OUTPUT" output)
    (is (= ["hey hey" "you" "there"] output))))

(deftest test-numbers
  (is (= 1 1)))


