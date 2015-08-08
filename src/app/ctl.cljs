(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform]]
            [app.datasource :as data]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]))

(defn reload-hook []
  (println "RELOAD CTL"))

(def controls-visible (atom true))
(def import-visible? (atom false))
(def dataview-visible? (atom false))
(def active-list-idx (atom 0))


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
  (for [item items] [:div  [:span item] [:button {:on-click #(put! eventbus-in {:delete item})} "D"]]))


(defn data-tab-item [data eventbus-in]
  (for [i (range (count data))] 
    [:div {:class (str "muted-" (:muted? (data i)) )}
     [:a {:data-idx i} (:title (data i))]
     [:span {:class (str "glyphicon " (if (:muted? (data i)) "glyphicon-volume-off" "glyphicon-volume-up")) 
             :aria-hidden "true"
             :on-click #(data/toggle-muted (data i))} ]]))

(defn control-panel [eventbus-in
                     playstate
                     playstates                     
                     playmode
                     config
                     randomize?
                     data
                     ]
  (when true
    (xform ctl-tpl 
           ["#import-dlg" {:style (display? import-visible?)}]
           ["#control-panel" {:class (clojure.string/lower-case (playstates @playstate))}]
           ["#playbutton" {:on-click #(toggleplay playstate eventbus-in)} ]
           ["#dataview" {:style (display? dataview-visible?)}]
           ["#dataview .datalist li" :* (data-item (:items (@data @active-list-idx)) eventbus-in) ]
           ["#dataview .nav-tabs li" :* (data-tab-item @data eventbus-in) ]
           ["#dataview .nav-tabs a" {:on-click #(let [t (.. % -target)
                                                      idx (.getAttribute t "data-idx")] 
                                                  (println "IDX" idx)
                                                  (reset! active-list-idx idx) )}]
           ["#ejectbutton" {:on-click #(reset! dataview-visible? (not @dataview-visible?))} ]
           ["#play-state" (playstates @playstate)]           
           ["#clear-screen" {:on-click #(put! eventbus-in :clear)}]
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
           )))
