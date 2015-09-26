(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform to-hiccup to-doc-frag]]
            [cljs.reader]
            [markdown.core :refer [md->html]]
            [cljsjs.mousetrap]
            [reagent.core :as reagent :refer [atom]]     
            [re-com.core  :refer [h-box v-box box gap line label checkbox 
                                  radio-button button single-dropdown
                                  input-textarea
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

(defn import-component-body-func [submit-dialog cancel-dialog dialog-data]  
  (fn []
    [v-box
     :children [[label
                 :label "Type (or paste) text into the text area (one item per line) and hit import."]
                [gap :size "15px"]
                [h-box
                 :children [[input-textarea
                             :model            dialog-data
                             :width            "300px"
                             :rows             10
                             :placeholder      "Enter items, one per line \n item"
                             :on-change        #(reset! dialog-data %)
                             :change-on-blur?  true]]]
                [gap :size "20px"]
                [line]
                [gap :size "10px"]
                [h-box
                 :gap      "10px"
                 :children [[button
                             :label    [:span [:i {:class "zmdi zmdi-check" }] " Apply"]
                             :on-click #(submit-dialog @dialog-data)
                             :class    "btn-primary"]]]]]))

(defn popover-body
  [showing? position dialog-data on-change]
  (let [dialog-data   (reagent/atom (deref-or-value dialog-data))
        submit-dialog (fn [new-dialog-data]
                        (reset! showing? false)
                        (on-change new-dialog-data))
        cancel-dialog #(reset! showing? false)]
    (fn []
      [popover-content-wrapper
       :showing?         showing?
       :on-cancel        cancel-dialog
       :position         position
       :width            "400px"
       :backdrop-opacity 0.3
       :title            "Import items"
       :body             [(import-component-body-func submit-dialog cancel-dialog dialog-data)]])))


(defn import-dlg [active-channel]
  (let [showing? (atom false)
        dlg-data (atom "")
        on-change #(dispatch-sync [:import % @active-channel])]
    (fn []
      [popover-anchor-wrapper
       :showing? showing?
       :position :right-below
       :anchor [button 
                :label "Import"
                :on-click #(reset! showing? true)]
       :popover [popover-body showing? :right-below dlg-data on-change]])))


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


(defn data-item [item channel]
  (let [editing (atom false)]
    (fn [item channel]  
      [:div {:class "channel-item"}  
       (if @editing
         [title-edit {:title item
                      :on-save #(dispatch-sync [:channel-update-item @channel item %])
                      :on-stop #(reset! editing false)}]
         [:span {:on-click #(reset! editing (not @editing))} item]) 
       [:button {:class "btn btn-xs"
                 :on-click #(dispatch-sync [:delete item @channel])} 
        [:span {:class "glyphicon glyphicon-remove"}]]])))

(defn data-items [items channel]
  [:ul        
   (for [item items] 
     [data-item item channel])])

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

(defn dataview [active-channel channels active-list-idx import-visible?]
  "Editing channels and data"
  (xform dataview-tpl
         [".datalist" {:on-drag-over allow-drop
                       :on-drag-enter allow-drop
                       :on-drop (fn [e] (.preventDefault e)
                                  (let [tree (.getData (.-dataTransfer e) "text")]
                                    (dispatch-sync [:import tree @active-channel])))}]
         [".datalist" [data-items (:items @active-channel) active-channel] ]
         [".nav-tabs li" :* (data-tab-item channels @active-list-idx) ]
         [".nav-tabs a" 
          ]
         ["#channel-controls .channel-mix"  
          {:value (:gain (@channels @active-list-idx))
           :on-change #(dispatch-sync 
                        [:channel-set-mix 
                         (@channels @active-list-idx) 
                         (cljs.reader/read-string (.. % -target -value))])}]
         ["#toggle-import-dlg" {:on-click #(dispatch-sync [:toggle-import-visibility])}]
         ["#import-dlg" {:class (visibility-class @import-visible?)}]
         [".buttons" :*> (list [:li [import-dlg active-channel]]
                               [:li [button 
                                     :label "Clear"
                                     :disabled? false
                                     :on-click #(dispatch-sync [:clear @active-channel])
                                     :tooltip "Remove all items from this channel"]]
                               [:li [button 
                                     :label "Clear all"
                                     :on-click #(dispatch-sync [:clear-all])
                                     :tooltip "Remove all items from all channels"]]
                               [:li [button
                                     :label "Export"
                                     :on-click #(dispatch-sync [:export-all])
                                     :tooltip "Export items from all channels as plain text list"]])]
         
         ["#textareaimport-button" 
          {:on-click 
           (fn [] 
             (dispatch-sync [:import
                             (.-value (.getElementById js/document "textareaimport"))
                             @active-channel])
             (aset (.getElementById js/document "textareaimport") "value" "")
             (dispatch-sync [:start]))}]
         ["#textarea-export" {:on-click (fn [e]
                                          (.focus (.-target e))
                                          (.select (.-target e)))}]))

(defn control-panel [playstates]
  (let [player (subscribe [:player])
        controls (subscribe [:controls])
        active-list-idx (reaction (:active-list-idx @controls))
        dataview-visible? (reaction (:dataview-visible? @controls))
        help-visible? (reaction (:help-visible? @controls))
        import-visible? (reaction (:import-visible? @controls))
        channels (subscribe [:channels])
        active-channel (reaction (@channels @active-list-idx))] 
    (fn []
      (xform ctl-tpl 

             ["#control-panel" {:class (clojure.string/lower-case (playstates (:playstate @player)))}]
             ["#playbutton" {:on-click #(dispatch-sync [:toggle-play])} ]
             ["#dataview" {:style (display? dataview-visible?)}]
             ["#dataview" :*> (dataview active-channel channels active-list-idx import-visible?)]

             ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]
             ["#play-state" (playstates (:playstate player))]

             ;; how to refer to the attrs of elements here?
             ["#playmode input.drizzle" (if (= (:playmode @player) "drizzle") {:checked "true"} {})]
             ["#playmode input.pairs" (if (= (:playmode @player) "pairs") {:checked "true"} {})]
             ["#playmode input.single" (if (= (:playmode @player) "single") {:checked "true"} {})]
                  
             ["#playmode input" 
              {:on-change (fn [e]
                            (dispatch-sync [:set-playmode 
                                            (.-value (.-target e))])
                            (dispatch-sync [:start]))}]
             
             ["#ipm" {:value (Math/floor (* 60  (:items-per-sec @player)))
                      :on-change (fn [evt] 
                                   (dispatch-sync [:set-ipm
                                                   (cljs.reader/read-string (.. evt -target -value ))])
                                   (dispatch-sync [:start]))}]
             ["#doRandomize" (if (:randomize? @player) {:checked "true"} {})]
                                        ;           ["#doRandomize" {:on-click #(println (.. % -target -checked))}]
             ["#doRandomize" {:on-click #(dispatch-sync [:set-randomize (.. % -target -checked)])}]
             [".help" {:on-click #(dispatch-sync [:toggle-help])}]
             ["#help" {:style (display? help-visible?)} ]
             ["#help" :*> (xform ( str "<div>" (md->html help-tpl) "</div>"))]))))

(.click (js/jQuery "#screen")
        (fn [evt]
          (.toggle (js/jQuery "nav"))))

(.click (js/jQuery "#controls-overlay")
        (fn [evt]
          (.toggle (js/jQuery "nav"))))


(.bind js/Mousetrap "space" #(dispatch-sync [:toggle-play]))
(.bind js/Mousetrap "i" #(dispatch-sync [:insert-mode-enable]))
(.bind js/Mousetrap "esc" #(dispatch-sync [:insert-mode-disable]))

