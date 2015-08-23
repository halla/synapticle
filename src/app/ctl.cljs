(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform]]
            [cljs.reader]
            [cljsjs.mousetrap]
            [reagent.core :as reagent :refer [atom]]     
            [re-frame.core :refer [dispatch-sync
                                   subscribe
                                   ]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]])
  (:use [domina.css :only [sel]]
        [domina.events :only [listen! target]]))

(defn reload-hook []
  (println "RELOAD CTL"))

(deftmpl ctl-tpl "controls.html")

(defn display? [visible?]
  (if @visible?
    "display: block;"
    "display: none;"))

(defn data-item [items channel]
  (for [item items] 
    [:div  
     [:span item] 
     [:button {:on-click #(dispatch-sync [:delete item channel])} "D"]]))

(defn data-tab-item [data active-idx]
  (for [i (range (count data))] 
    [:div {:class (str "muted-" (:muted? (data i)) (when (= active-idx i) " active"))}
     [:a {:data-idx i} (:title (data i))]
     [:span {:class (str "glyphicon " (if (:muted? (data i)) "glyphicon-volume-off" "glyphicon-volume-up")) 
             :aria-hidden "true"
             :on-click #(dispatch-sync [:mute (data i)])} ]]))

(defn control-panel [playstates]
  (let [player (subscribe [:player])
        controls (subscribe [:controls])
        active-list-idx (reaction (:active-list-idx @controls))
        dataview-visible? (reaction (:dataview-visible? @controls))
        import-visible? (reaction (:import-visible? @controls))
        channels (subscribe [:channels])
        active-channel (reaction (@channels @active-list-idx))] 
    
    (fn []
      (xform ctl-tpl 
             ["#import-dlg" {:style (display?  import-visible?)}]
             ["#control-panel" {:class (clojure.string/lower-case (playstates (:playstate @player)))}]
             ["#playbutton" {:on-click #(dispatch-sync [:toggle-play])} ]
             ["#dataview" {:style (display? dataview-visible?)}]
             ["#channel-controls .channel-mix"  {:value (:gain (@channels @active-list-idx))
                                                 :on-change #(dispatch-sync 
                                                              [:channel-set-mix (@channels @active-list-idx) (cljs.reader/read-string (.. % -target -value))])}]
             ["#dataview .datalist li" :* (data-item (:items @active-channel) @active-channel) ]
             ["#dataview .nav-tabs li" :* (data-tab-item @channels @active-list-idx) ]
             ["#dataview .nav-tabs a" {:on-click #(let [t (.. % -target)
                                                        idx (cljs.reader/read-string (.getAttribute t "data-idx"))] 
                                                    (dispatch-sync [:set-active-channel idx]) )}]
             ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]
             ["#play-state" (playstates (:playstate player))]           
             ["#clear-screen" {:on-click #(dispatch-sync [:clear @active-channel])}]
             ["#toggle-import-dlg" {:on-click #(dispatch-sync [:toggle-import-visibility])}]
             ["#playmode input.drizzle" (if (= (:playmode @player) "drizzle") {:checked "true"} {})]
             ["#playmode input.pairs" (if (= (:playmode @player) "pairs") {:checked "true"} {})]
             ["#playmode input.single" (if (= (:playmode @player) "single") {:checked "true"} {})]
             ["#textareaimport-button" {:on-click 
                                        (fn [] 
                                          (dispatch-sync [:import
                                                          (.-value (.getElementById js/document "textareaimport"))
                                                          @active-channel])
                                          (aset (.getElementById js/document "textareaimport") "value" "")
                                          (dispatch-sync [:start])
                                          
                                                        )}]           
             ["#playmode .drizzle" {:on-change (fn [] 
                                                 (dispatch-sync [:set-playmode "drizzle"])
                                                 (dispatch-sync [:start]))}]
             ["#playmode .pairs" {:on-change (fn [] 
                                               (dispatch-sync [:set-playmode "pairs"])
                                               (dispatch-sync [:start]))}]
             ["#playmode .single" {:on-change (fn [] 
                                                (dispatch-sync [:set-playmode "single"])
                                                (dispatch-sync [:start]))}]
             ["#ipm" {:value (Math/floor (* 60  (:items-per-sec @player)))
                      :on-change (fn [evt] 
                                   (dispatch-sync [:set-ipm
                                                   (cljs.reader/read-string (.. evt -target -value ))])
                                   (dispatch-sync [:start]))}]
             ["#doRandomize" (if (:randomize? @player) {:checked "true"} {})]
                                        ;           ["#doRandomize" {:on-click #(println (.. % -target -checked))}]
             ["#doRandomize" {:on-click #(dispatch-sync [:set-randomize (.. % -target -checked)])}]
             ))))

(listen! (sel "#screen") :click 
         (fn [evt]
           (.toggle (js/jQuery "nav"))))

;(.addEventListener js/document)
#_(listen! :keydown
         (fn [evt]
           (enable-console-print!)
            (.toggle (js/jQuery "nav"))))


(.bind js/Mousetrap "space" #(dispatch-sync [:toggle-play]))


