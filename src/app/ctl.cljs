(ns app.ctl
  (:require [cljs.core.async :refer [put! chan <! mult tap]]
            [dragonmark.web.core :as dw :refer [xf xform]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [cljs.core.async.macros :refer [go]]))

(defn reload-hook []
  (println "RELOAD CTL")
)
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


(defn control-panel [eventbus-in
                     playstate
                     playstates
                     controls-visible?
                     playmode
                     config
                     randomize?
                     ]
  (when true
    (xform ctl-tpl 
           ["#control-panel" {:style (display? controls-visible?)}]
           ["#control-panel" {:class (clojure.string/lower-case (playstates @playstate))}]
           ["#playbutton" {:on-click #(toggleplay playstate eventbus-in)} ]
           ["#play-state" (playstates @playstate)]           
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