(ns app.screen-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [app.screen :as screen]))


;; TODO generative testing 

(deftest test-collides
  (let [div1 {:x 100 :y 100}
        div2 {:x 100 :y 100}]
    (is (= true (screen/collides? div1 div2)))))

(deftest test-collides2
  (let [div1 {:x 100 :y 100}
        div2 {:x 1000 :y 1000}]
    (is (= false (screen/collides? div1 div2)))))

(deftest test-collides3
  (let [div1 {:x 110 :y 100}
        div2 {:x 100 :y 110}]
    (is (= true (screen/collides? div1 div2)))))

(deftest test-collides4
  (let [div1 {:x 110 :y 110}
        div2 {:x 100 :y 100}]
    (is (= true (screen/collides? div1 div2)))))
