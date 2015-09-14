(ns app.subscribables
  (:require [re-frame.core :refer [register-sub]]
            [app.datasource.subs])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; re-frame style subs

(register-sub :controls (fn [db _] (reaction (:controls @db))))

(register-sub :player (fn [db _] (reaction (:player @db))))

(register-sub :channels (fn [db _] (reaction (:channels @db))))

(register-sub :screen (fn [db _] (reaction (:screen @db))))

