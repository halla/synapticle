(ns app.datasource.subs
  (:require [re-frame.core :refer [register-sub]])
  (:require-macros [reagent.ratom :refer [reaction]]))


(register-sub :datasets (fn [db _] (reaction (:datasets @db))))
