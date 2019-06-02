(ns app.view.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform to-hiccup to-doc-frag]]
            [cljs.reader]
            [markdown.core :refer [md->html]]
            [cljsjs.mousetrap]
            [app.imports.view :as imports]       
            [reagent.core :as reagent :refer [atom]]
            [re-com.core  :refer [h-box v-box box gap line label checkbox 
                                  radio-button button single-dropdown
                                  input-textarea modal-panel slider
                                  popover-content-wrapper popover-anchor-wrapper]]
            [re-com.util :refer [deref-or-value]]
            [re-frame.core :refer [dispatch-sync
                                   subscribe
                                   ]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]))

(defn reload-hook []
  (println "RELOAD CTL"))

(deftmpl ctl-tpl "controls.html")

(deftmpl help-tpl "help.html")


(defn help-dlg
  "Overlay help text"
  []
  (let [show? (reagent/atom false)]
    (fn []
      [v-box
       :children [[:span {:class "help glyphicon glyphicon-question-sign"
                          :on-click #(reset! show? true)}]
                  (when @show?
                    [modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child             
                     (xform (str "<div>" (md->html help-tpl) "</div>"))])]])))


(defn ipm-slider [model]
  [slider
   :model     model
   :min       1
   :max       400
   :step      10
   :class "form-control"
   :width     "150px"
   :on-change #(do (dispatch-sync [:set-ipm (str %)])
                   (dispatch-sync [:start]))
   :disabled? false])

(defn playmode-selector [model]
  [:div {:class "form-group"}
   (list (doall (for [mode ["drizzle" "pairs" "single" "grid"]]
                  ^{:key mode}
                  [radio-button 
                   :label (clojure.string/capitalize mode)
                   :value mode
                   :model model
                   :on-change (fn [e]
                                (dispatch-sync [:set-playmode mode]))])))])

(defn navbar-view [player]
  (let [ipm (reaction (* 60  (:items-per-sec @player)))
        playmode (reaction (:playmode @player))]
    (list
     [playmode-selector playmode]
     [:div {:class "form-group"}
      [:label "Speed:"]
      [ipm-slider ipm]]

     [:div {:class "form-group"} 
      [:span {:class "glyphicon glyphicon-fullscreen"
              :on-click #(dispatch-sync [:toggle-distraction-free-mode])}]]
     [:div {:class "form-group"}
      [help-dlg]])))

(defn control-panel []
  (let [player (subscribe [:player])
        controls (subscribe [:controls])
        distraction-free? (reaction (:distraction-free-mode? @controls))] 
 
   (fn []
      (when (not @distraction-free?)
        (xform ctl-tpl             
               ["#control-panel" {:class (name (:playstate @player))}]
               ["#playbutton" {:on-click #(dispatch-sync [:toggle-play])} ]
               ["#stepbutton" {:on-click #(dispatch-sync [:step])}]
               ["#wordinputs" [app.imports.view/textfield-component]] 

               ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]

               ["#doRandomize" (if (:randomize? @player) {:checked "true"} {})]
               ["#doRandomize" {:on-click #(dispatch-sync [:set-randomize 
                                                           (.. % -target -checked)])}]
               ["#control-panel .row.navi" :*> (navbar-view player) ])))))


(.click (js/jQuery "#screen")
        (fn [evt]
          (dispatch-sync [:toggle-distraction-free-mode])))


(.click (js/jQuery "#controls-overlay")
        (fn [evt]
          (dispatch-sync [:toggle-distraction-free-mode])))


(.bind js/Mousetrap "space" #(dispatch-sync [:toggle-play]))
(.bind js/Mousetrap "i" #(dispatch-sync [:insert-mode-enable]))
(.bind js/Mousetrap "esc" #(dispatch-sync [:insert-mode-disable]))
(.bind js/Mousetrap "right" #(dispatch-sync [:step]))

