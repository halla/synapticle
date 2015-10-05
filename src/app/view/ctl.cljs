(ns app.view.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform to-hiccup to-doc-frag]]
            [cljs.reader]
            [markdown.core :refer [md->html]]
            [cljsjs.mousetrap]
            [app.imports.view :as imports]
            [app.view.exports :as exports]
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

(deftmpl dataview-tpl "dataview.html")


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



(defn title-input [{:keys [title on-save on-stop]}]
  (let [val (atom title)
        stop #(do (on-stop)
                  (reset! val ""))
        save #(let [v (clojure.string/trim @val)] 
                (on-save v)
                (stop))]
    (fn [{:keys [title on-save on-stop]}]
      [:input {:value @val
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def title-edit (with-meta title-input
                  {:component-did-mount #(do (.focus (reagent/dom-node %))
                                             (.select (reagent/dom-node %)))}))



(defn display? [visible?]
  (if @visible?
    "display: block;"
    "display: none;"))

(defn visibility-class [visible?]
  (if visible?
    ""
    "hidden"))


(defn channel-item 
  "Single item in single channel. Editable, deletable."
 [item channel]
  (let [editing (atom false)]
    (fn [item channel]  
      [:div {:class "channel-item"}  
       (if @editing
         [title-edit {:title item
                      :on-save #(dispatch-sync [:channel-update-item @channel item %])
                      :on-stop #(reset! editing false)}]
         [:span {:on-click #(reset! editing (not @editing))
                 :class "text"} item]) 
       [:button {:class "btn btn-xs"
                 :on-click #(dispatch-sync [:delete item @channel])}

        [:a {:class "delete"}  "\u274c" ] ]])))

(defn channel-items [items channel]
  [:ul        
   (for [item items] 
     [channel-item item channel])])

(defn channel-title [{:keys [title i channels]}]
  (let [editing (atom false)]
    (fn [{:keys [title i channels]}]
      [:a {:data-idx i
           :on-click #(dispatch-sync [:set-active-channel i])
           :on-double-click #(reset! editing (not @editing))}
       (if @editing
         [title-edit {:title (:title (@channels i))
                       :on-save #(dispatch-sync [:channel-set-title i %])
                       :on-stop #(reset! editing false)}]
         [:span {:class (str "view" @editing)} (:title (@channels i))])])))

(defn data-tab-item [channels active-idx]
  (let [data @channels]
    (for [i (range (count data))] 
      [:div {:class (str "muted-" (:muted? (data i)) (when (= active-idx i) " active"))}
       [channel-title {:title (:title (@channels i))
                       :i i
                       :channels channels}]
       [:span {:class (str "glyphicon " (if (:muted? (data i)) "glyphicon-volume-off" "glyphicon-volume-up")) 
               :aria-hidden "true"
               :on-click #(dispatch-sync [:mute (data i)])} ]])))

(defn allow-drop [e]
  (.preventDefault e))

(defn dataview [active-channel channels active-list-idx]
  "Editing channels and data"
  (xform dataview-tpl
         [".datalist" {:on-drag-over allow-drop
                       :on-drag-enter allow-drop
                       :on-drop (fn [e]
                                  (.preventDefault e)
                                  (let [tree (.getData (.-dataTransfer e) "text")]
                                    (dispatch-sync [:import tree @active-channel])))}]
         [".datalist" :* [channel-items (:items @active-channel) active-channel] ]
         [".nav-tabs li" :* (data-tab-item channels @active-list-idx) ]
         [".channels.buttons" :*> (list 
                                  [:li [exports/export-dlg channels]]
                                  [:li [button 
                                     :label "Clear all"
                                     :class "btn-default btn-sm"
                                     :on-click #(dispatch-sync [:clear-all])
                                     :tooltip "Remove all items from all channels"]])]
         ["#channel-controls .channel-mix"  
          {:value (:gain (@channels @active-list-idx))
           :on-change #(dispatch-sync 
                        [:channel-set-mix 
                         (@channels @active-list-idx) 
                         (cljs.reader/read-string (.. % -target -value))])}]
         ["#channel-controls" :*> (list [:li [imports/import-dlg active-channel]]
                                        [:li [button 
                                     :label "Clear"
                                     :class "btn-default btn-sm"
                                     :disabled? false
                                     :on-click #(dispatch-sync [:clear @active-channel])
                                     :tooltip "Remove all items from this channel"]]
                               )]))

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
   (list (doall (for [mode ["drizzle" "pairs" "single"]]
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
     [:div {:class "form-control"}
      [:label "Speed:"]
      [ipm-slider ipm]]
     [:div {:class "form-group"} [help-dlg]])))

(defn control-panel [playstates]
  (let [player (subscribe [:player])
        controls (subscribe [:controls])
        active-list-idx (reaction (:active-list-idx @controls))
        dataview-visible? (reaction (:dataview-visible? @controls))
        channels (subscribe [:channels])
        active-channel (reaction (@channels @active-list-idx))] 
    (fn []
      (xform ctl-tpl             
             ["#control-panel" {:class (clojure.string/lower-case (playstates (:playstate @player)))}]
             ["#playbutton" {:on-click #(dispatch-sync [:toggle-play])} ]
             ["#wordinputs" [app.imports.view/textfield-component]] 

             ["#dataview" {:style (display? dataview-visible?)}]
             ["#dataview" :*> (dataview active-channel channels active-list-idx)]

             ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]
             ["#play-state" (playstates (:playstate player))]

             ["#doRandomize" (if (:randomize? @player) {:checked "true"} {})]
             ["#doRandomize" {:on-click #(dispatch-sync [:set-randomize (.. % -target -checked)])}]
             ["#control-panel .row.navi" :*> (navbar-view player) ]))))


(.click (js/jQuery "#screen")
        (fn [evt]
          (.toggle (js/jQuery "nav"))))

(.click (js/jQuery "#controls-overlay")
        (fn [evt]
          (.toggle (js/jQuery "nav"))))


(.bind js/Mousetrap "space" #(dispatch-sync [:toggle-play]))
(.bind js/Mousetrap "i" #(dispatch-sync [:insert-mode-enable]))
(.bind js/Mousetrap "esc" #(dispatch-sync [:insert-mode-disable]))

