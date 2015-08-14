(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform]]
            [app.datasource :as data]
            [cljs.reader]
            [reagent.core :as reagent :refer [atom]]            
            [re-frame.core :refer [dispatch-sync
                                   subscribe]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]))


(defn reload-hook []
  (println "RELOAD CTL"))

(deftmpl ctl-tpl "controls.html")

(defn display? [visible?]
  (if @visible?
    "display: block;"
    "display: none;"))

(defn data-item [items]
  (for [item items] [:div  [:span item] [:button {:on-click #(dispatch-sync [:delete item])} "D"]]))

(defn data-tab-item [data active-idx]
  (for [i (range (count data))] 
    [:div {:class (str "muted-" (:muted? (data i)) (when (= active-idx i) " active"))}
     [:a {:data-idx i} (:title (data i))]
     [:span {:class (str "glyphicon " (if (:muted? (data i)) "glyphicon-volume-off" "glyphicon-volume-up")) 
             :aria-hidden "true"
             :on-click #(data/toggle-muted (data i))} ]]))

(defn control-panel [playstates                     
                     data]
  (let [active-list-idx (subscribe [:active-list-idx])
        items-per-sec (subscribe [:items-per-sec])
        playmode (subscribe [:playmode])
        playstate (subscribe [:playstate])
        randomize? (subscribe [:randomize])
        dataview-visible? (subscribe [:dataview-visible?])
        import-visible? (subscribe [:import-visible?])]
    (fn []
      (xform ctl-tpl 
             ["#import-dlg" {:style (display? import-visible?)}]
             ["#control-panel" {:class (clojure.string/lower-case (playstates @playstate))}]
             ["#playbutton" {:on-click #(dispatch-sync [:toggle-play])} ]
             ["#dataview" {:style (display? dataview-visible?)}]
             ["#channel-controls .channel-mix"  {:value (:gain (@data @active-list-idx))
                                                 :on-change #(dispatch-sync 
                                                              [:channel-set-mix data (cljs.reader/read-string (.. % -target -value))])}]
             ["#dataview .datalist li" :* (data-item (:items (@data @active-list-idx))) ]
             ["#dataview .nav-tabs li" :* (data-tab-item @data @active-list-idx) ]
             ["#dataview .nav-tabs a" {:on-click #(let [t (.. % -target)
                                                        idx (cljs.reader/read-string (.getAttribute t "data-idx"))] 
                                                    (dispatch-sync [:set-active-channel idx]) )}]
             ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]
             ["#play-state" (playstates @playstate)]           
             ["#clear-screen" {:on-click #(dispatch-sync [:clear])}]
             ["#toggle-import-dlg" {:on-click #(dispatch-sync [:toggle-import-visibility])}]
             ["#playmode input.drizzle" (if (= @playmode "drizzle") {:checked "true"} {})]
             ["#playmode input.pairs" (if (= @playmode "pairs") {:checked "true"} {})]
             ["#playmode input.single" (if (= @playmode "single") {:checked "true"} {})]
             ["#textareaimport-button" {:on-click #(dispatch-sync [:textarea-import])}]           
             ["#playmode .drizzle" {:on-change (fn [] 
                                                 (dispatch-sync [:set-playmode "drizzle"])
                                                 (dispatch-sync [:start]))}]
             ["#playmode .pairs" {:on-change (fn [] 
                                               (dispatch-sync [:set-playmode "pairs"])
                                               (dispatch-sync [:start]))}]
             ["#playmode .single" {:on-change (fn [] 
                                                (dispatch-sync [:set-playmode "single"])
                                                (dispatch-sync [:start]))}]
             ["#ipm" {:value (Math/floor (* 60  @items-per-sec))
                      :on-change (fn [evt] (dispatch-sync [:set-ipm 
                                                           (cljs.reader/read-string (.. evt -target -value ))]))}]
             ["#doRandomize" (if @randomize? {:checked "true"} {})]
                                        ;           ["#doRandomize" {:on-click #(println (.. % -target -checked))}]
             ["#doRandomize" {:on-click #(dispatch-sync [:set-randomize (.. % -target -checked)])}]
             ))))
