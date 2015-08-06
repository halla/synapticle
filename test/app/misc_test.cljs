(ns app.misc-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [app.importer :as importer]))


(deftest test-split-by-line
  (let [input ["hey\nyou\nthere"]
        output (importer/split-by-line input)
        ]
    (is (= ["hey" "you" "there"] output))))


(deftest test-split-by-line-single
  (let [input "hey\nyou\nthere"
        output (importer/split-by-line input)
        ]
    (is (= ["hey" "you" "there"] output))))

(deftest test-process-input
  (let [input " hey hey\n  you  \nthere "
        output (importer/process-input input)]
    (println "OUTPUT" output)
    (is (= ["hey hey" "you" "there"] output))))

(deftest test-numbers
  (is (= 1 1)))

(deftest test-numbers2
  (is (= 1 1)))
