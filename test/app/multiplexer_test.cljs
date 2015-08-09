(ns app.multiplexer-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [app.utils :as u]
            [app.multiplexer :as mux]))




(deftest dont-mix-muted-sources
  (let [sources (atom [{:items ["asdf" "aqwer" "azxcv"]
                        :muted? false}
                       {:items ["qwer"]
                        :muted? true}])
        items (take 100 (repeatedly #(mux/get-item sources)))]
    (is (= false (u/in? items "qwer")))))
