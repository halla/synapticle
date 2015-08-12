(ns app.handlers
  (:require [re-frame.core :refer [register-handler]]
            [app.db :as db]
            [app.screen :as screen]
            [app.datasource :as data]
            [app.ctl :as ctl]))

;: -- re-frame style handlers

(register-handler :initialize-db (fn [_ _] db/default-value))

(register-handler :toggle-dataview-visibility (fn [db _] (update-in db [:dataview-visible?] #(not %))))

;; todo decouple this
(register-handler :clear (fn [db _] 
  (data/clear (@data/wordlists @ctl/active-list-idx))
  (screen/clear)
  db))
