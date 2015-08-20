(ns app.subscribables
  (:require [re-frame.core :refer [register-sub]])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; re-frame style subs

(register-sub :controls (fn [db _] (reaction (:controls @db))))
(register-sub :dataview-visible? (fn [db _]  (reaction (get-in @db [:controls :dataview-visible?]))))
(register-sub :import-visible? (fn [db _]  (reaction (get-in @db [:controls :import-visible?]))))
(register-sub :active-list-idx (fn [db _]  (reaction (get-in @db [:controls :active-list-idx]))))

(register-sub :player (fn [db _] (reaction (:player @db))))
(register-sub :items-per-sec (fn [db _] (reaction (get-in @db [:player :items-per-sec]))))
(register-sub :playmode (fn [db _] (reaction (get-in @db [:player :playmode]))))
(register-sub :playstate (fn [db _] (reaction (get-in @db [:player :playstate]))))
(register-sub :randomize (fn [db _] (reaction (get-in @db [:player :randomize]))))

(register-sub :channels (fn [db _] (reaction (:channels @db))))

