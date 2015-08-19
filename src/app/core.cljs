(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch-sync]]
            [app.importer :as importer]
            [app.datasource :as data]
            [app.ctl :as ctl]
            [app.views :as views]
            [app.handlers :as handlers]
            [app.subscribables]
            [cljsjs.jquery]))


(enable-console-print!)

(defn main []
  "Called on code reload"
  (reagent.core/flush)
  (println "RELOAD"))


(dispatch-sync [:initialize-db])


(defn mount-root []
  (reagent/render [ctl/control-panel
                   handlers/playstates                  
                   ] (.getElementById js/document "controls"))
  (reagent/render [importer/textfield-component]
                  (.getElementById js/document "wordinputs"))
  (reagent/render [views/render-player] (.getElementById js/document "screen"))
  )


(mount-root)
