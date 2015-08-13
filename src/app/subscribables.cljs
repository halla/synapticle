(ns app.subscribables
  (:require [re-frame.core :refer [register-sub]])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; re-frame style subs

(register-sub :dataview-visible? (fn [db _]  (reaction (:dataview-visible? @db))))
(register-sub :active-list-idx (fn [db _]  (reaction (:active-list-idx @db))))
(register-sub :items-per-sec (fn [db _] (reaction (:items-per-sec @db))))
