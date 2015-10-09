(ns app.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]     
            [app.view.ctl :as ctl]
            [app.views :as views]
            [app.handlers :as handlers]
            [app.channels.view :as channels]
            [app.player.handlers :as playerhandlers]
            [app.datasource.views]
            [app.subscribables]
            [cljsjs.jquery]))



(enable-console-print!)



(dispatch-sync [:initialize-db])


(defn mount-root []
  (reagent/render [ctl/control-panel] 
                  (.getElementById js/document "controls"))

  (reagent/render [channels/channel-editor]
                  (.getElementById js/document "channel-editor"))

  (reagent/render [app.imports.view/textfield-component]
                  (.getElementById js/document "controls-overlay"))

  (reagent/render [views/render-player] 
                  (.getElementById js/document "screen"))
  
  
  (reagent/render [app.datasource.views/browser]
                  (.getElementById js/document "data-browser")))


(mount-root)

(defn main []
  "Called on code reload"
  (dispatch-sync [:start])
  (println "RELOAD")
  (mount-root))
