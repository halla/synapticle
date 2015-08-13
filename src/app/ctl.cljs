(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform]]
            [app.datasource :as data]
            [cljs.reader]
            [reagent.core :as reagent :refer [atom]]            
            [re-frame.core :refer [register-handler 
                                   register-sub
                                   dispatch-sync
                                   subscribe]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]
                   [reagent.ratom :refer [reaction]]))


(defn reload-hook []
  (println "RELOAD CTL"))

(def import-visible? (atom false))

(defn start [eventbus-in]
  (put! eventbus-in :start))

(defn stop [eventbus-in]
  (put! eventbus-in :stop))

(defn restart [eventbus-in]
  (put! eventbus-in :restart))

(defn toggleplay [playstate eventbus-in]
  (if (= @playstate :stopped)
    (start eventbus-in)
    (stop eventbus-in)))


(deftmpl ctl-tpl "controls.html")

(defn set-playmode! [eventbus-in playmode mode]
  (reset! playmode mode)
  (restart eventbus-in))

(defn set-ipm! [eventbus-in config ipm]
  (println "IPM" ipm)
  (swap! config assoc :items-per-sec (/ ipm 60))
  (restart eventbus-in))

(defn set-randomize [eventbus-in randomize? value]
  (reset! randomize? value)
  (restart eventbus-in))

(defn display? [visible?]
  (println "VISIBLE" @visible?)
  (if @visible?
    "display: block;"
    "display: none;"))

(defn data-item [items eventbus-in]
  (for [item items] [:div  [:span item] [:button {:on-click #(dispatch-sync [:delete item])} "D"]]))


(defn data-tab-item [data active-idx eventbus-in]
  (for [i (range (count data))] 
    [:div {:class (str "muted-" (:muted? (data i)) (when (= active-idx i) " active"))}
     [:a {:data-idx i} (:title (data i))]
     [:span {:class (str "glyphicon " (if (:muted? (data i)) "glyphicon-volume-off" "glyphicon-volume-up")) 
             :aria-hidden "true"
             :on-click #(data/toggle-muted (data i))} ]]))

(register-sub :dataview-visible? (fn [db _]  (reaction (:dataview-visible? @db))))
(register-sub :active-list-idx (fn [db _]  (reaction (:active-list-idx @db))))
;todo switch to using app-db, move to handlers.cljs, 


(register-handler :set-active-channel (fn [db [_ idx]]  (assoc-in db [:active-list-idx] idx) ))

(register-handler :toggle-play (fn [db [_ playstate eventbus-in]] (toggleplay playstate eventbus-in) db))
(register-handler :channel-set-mix (fn 
                                     [db [_ data value]] 
                                     (data/set-mix! 
                                      (@data (:active-list-idx db)) 
                                      value)
                                     db))

(register-handler :delete (fn [db [_ item]] 
                            (data/delete! (:active-list-idx db) item)
                            db))

(defn dataview-visibility []
  (let [visible? (subscribe [:dataview-visible?])]
    {:style (display? visible?)}))

(defn control-panel [eventbus-in
                     playstate
                     playstates                     
                     playmode
                     config
                     randomize?
                     data]
  (let [active-list-idx (subscribe [:active-list-idx])]
    (fn []
      (xform ctl-tpl 
             ["#import-dlg" {:style (display? import-visible?)}]
             ["#control-panel" {:class (clojure.string/lower-case (playstates @playstate))}]
             ["#playbutton" {:on-click #(dispatch-sync [:toggle-play playstate eventbus-in])} ]
             ["#dataview" (dataview-visibility)]
             ["#channel-controls .channel-mix"  {:value (:gain (@data @active-list-idx))
                                                 :on-change #(dispatch-sync 
                                                              [:channel-set-mix data (cljs.reader/read-string (.. % -target -value))])}]
             ["#dataview .datalist li" :* (data-item (:items (@data @active-list-idx)) eventbus-in) ]
             ["#dataview .nav-tabs li" :* (data-tab-item @data @active-list-idx eventbus-in) ]
             ["#dataview .nav-tabs a" {:on-click #(let [t (.. % -target)
                                                        idx (cljs.reader/read-string (.getAttribute t "data-idx"))] 
                                                    (dispatch-sync [:set-active-channel idx]) )}]
             ["#ejectbutton" {:on-click #(dispatch-sync [:toggle-dataview-visibility])} ]
             ["#play-state" (playstates @playstate)]           
             ["#clear-screen" {:on-click #(dispatch-sync [:clear])}]
             ["#toggle-import-dlg" {:on-click #(reset! import-visible? (not @import-visible?))}]
             ["#playmode input.drizzle" (if (= @playmode "drizzle") {:checked "true"} {})]
             ["#playmode input.pairs" (if (= @playmode "pairs") {:checked "true"} {})]
             ["#playmode input.single" (if (= @playmode "single") {:checked "true"} {})]
             ["#textareaimport-button" {:on-click #(put! eventbus-in :textarea-import)}]           
             ["#ipm" {:value (Math/floor (* 60  (:items-per-sec @config)))}]
             ["#playmode .drizzle" {:on-change #(set-playmode! eventbus-in playmode "drizzle")}]
             ["#playmode .pairs" {:on-change #(set-playmode! eventbus-in playmode "pairs")}]
             ["#playmode .single" {:on-change #(set-playmode! eventbus-in playmode "single")}]
             ["#ipm" {:on-input (fn [evt] (set-ipm! eventbus-in config (.. evt -target -value )))}]
             ["#doRandomize" (if @randomize? {:checked "true"} {})]
                                        ;           ["#doRandomize" {:on-click #(println (.. % -target -checked))}]
             ["#doRandomize" {:on-click #(set-randomize eventbus-in randomize? (.. % -target -checked))}]
             ))))
