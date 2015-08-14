(ns app.subscribables
  (:require [re-frame.core :refer [register-sub]])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; re-frame style subs

(register-sub :dataview-visible? (fn [db _]  (reaction (:dataview-visible? @db))))
(register-sub :import-visible? (fn [db _]  (reaction (:import-visible? @db))))
(register-sub :active-list-idx (fn [db _]  (reaction (:active-list-idx @db))))
(register-sub :items-per-sec (fn [db _] (reaction (:items-per-sec @db))))
(register-sub :playmode (fn [db _] (reaction (:playmode @db))))
(register-sub :playstate (fn [db _] (reaction (:playstate @db))))
(register-sub :randomize (fn [db _] (reaction (:randomize @db))))

