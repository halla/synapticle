(ns app.handlers
  (:require [re-frame.core :refer [register-handler]]
            [app.db :as db]))

;: -- re-frame style handlers

(register-handler :initialize-db (fn [_ _] db/default-value))

(register-handler :toggle-dataview-visibility (fn [db _] (update-in db [:dataview-visible?] #(not %))))
