(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform to-hiccup to-doc-frag]]
            [cljs.reader]
            [markdown.core :refer [md->html]]
            [cljsjs.mousetrap]
            [reagent.core :as reagent :refer [atom]]     
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


(defn title-input [{:keys [title on-save on-stop]}]
  (let [val (atom title)
        stop #(do (on-stop)
                  (reset! val ""))
        save #(let [v (clojure.string/trim @val)] 
                (on-save v)
                (stop))]
    (fn []
      [:input {:value @val
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def title-edit (with-meta title-input
                 {:component-did-mount #(.focus (reagent/dom-node %))}))


(defn display? [visible?]
  (if @visible?
    "display: block;"
    "display: none;"))

(defn visibility-class [visible?]
  (if visible?
    ""
    "hidden"))


(defn data-item [item channel]
  (let [editing (atom true)
        channels (subscribe [:channels])]
    (fn []      
      @channels
      [:div  
       (if @editing
         [title-edit {:title item
                      :on-save #(dispatch-sync [:channel-update-item @channel item %])
                      :on-stop #(reset! editing false)}]
         [:span {:on-click #(reset! editing (not @editing))} item]) 
       [:button {:on-click #(dispatch-sync [:delete item @channel])} "D"]])))

(defn data-items [items channel]
  (let [items (reaction (:items @channel))] ;;items update but not on the screen for some reason
    (fn []
      [:ul        
       (for [item @items] 
         [data-item item channel])])))

(defn channel-title [{:keys [title i channels]}]
  (let [editing (atom false)]
    (fn []
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
         ["#clear-screen" {:on-click #(dispatch-sync [:clear @active-channel])}]
         ["#clear-all" {:on-click #(dispatch-sync [:clear-all])}]
         ["#toggle-import-dlg" {:on-click #(dispatch-sync [:toggle-import-visibility])}]


         ["#import-dlg" {:class (visibility-class @import-visible?)}]
         ["#textareaimport-button" 
          {:on-click 
           (fn [] 
             (dispatch-sync [:import
                             (.-value (.getElementById js/document "textareaimport"))
                             @active-channel])
             (aset (.getElementById js/document "textareaimport") "value" "")
             (dispatch-sync [:start]))}]
         ["#export-all" {:on-click (fn []                                          
                                     (dispatch-sync [:export-all]))}]
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

