(ns app.middleware
  (:require [schema.core :as s :include-macros true]
            [app.db :as db]
            [re-frame.core :refer [after path
                                   trim-v debug]]))


;; re-frame handler middleware 


(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

;; after an event handler has run, this middleware can check that
;; it the value in app-db still correctly matches the schema.
(def check-schema-mw (after (partial check-and-throw db/schema)))


(def ->ls (after db/db->ls!))


(def controls-mw [check-schema-mw
                  ->ls
                  (path :controls)
                  trim-v])

(def channel-mw [check-schema-mw
                 ->ls
                 (path :channels)
                 trim-v])
