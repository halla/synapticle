(ns app.core-test
 
  (:require [app.core :as core]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [cljs.core.async :refer [put! chan <! mult tap]]
            [cljs.test.check :as tc]
            [cljs.test.check.generators :as gen]
;            [clojure.test.check.rose-tree :as rose]

            [cljs.test.check.properties :as prop :include-macros true])
  (:require-macros [cljs.test.check.cljs-test :refer [defspec]]))


#_(def sort-idempotent-prop
  (prop/for-all [v (gen/vector gen/int)]
    (= (sort v) (sort (sort v)))))

#_(tc/quick-check 100 sort-idempotent-prop)

(defspec first-element-is-min-after-sorting ;; the name of the test
         100 ;; the number of iterations for test.check to test
         (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
           (= (apply min v)
              (first (sort v)))))

(defspec start-stop
  1 ;; something funny going on here, timers keep on running afterwards
  (prop/for-all [cmd (gen/elements [:start :stop :restart])]
                (put! core/eventbus-in cmd)
                (put! core/eventbus-in :start)
                (= :running @core/playstate)
                ))

