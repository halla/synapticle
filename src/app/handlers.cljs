(ns app.handlers
  (:require [re-frame.core :refer [register-handler 
                                   after path
                                   trim-v debug]]
            [schema.core :as s :include-macros true]
            [app.db :as db]
            [app.importer :as importer]
            [app.ctl :as ctl]
            [app.player.handlers])) ;;invoke player handlers


(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems  (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

;; after an event handler has run, this middleware can check that
;; it the value in app-db still correctly matches the schema.
(def check-schema-mw (after (partial check-and-throw db/schema)))


(def ->ls (after db/db->ls!))


(def controls-mw [check-schema-mw
                  ->ls
                  (path :controls)
                  trim-v])

(def channel-mw [check-schema-mw
                 ->ls
                 (path :channels)
                 trim-v])


;: -- re-frame style handlers

(register-handler 
 :initialize-db 
 [debug]
 (fn [_ _] (merge db/default-value (db/ls->db))))





;; ----- Controls 

(register-handler 
 :toggle-dataview-visibility 
 controls-mw
 (fn [controls _] (update-in controls [:dataview-visible?] #(not %))))

(register-handler 
 :toggle-import-visibility 
 controls-mw
 (fn [db _] (update-in db [:import-visible?] #(not %))))

(register-handler 
 :set-active-channel 
 controls-mw
 (fn [db [idx]]  (assoc-in db [:active-list-idx] idx) ))

(defn focus-text-input! []
  (-> (js/jQuery "#controls-overlay input")
      (.focus)
      (.val "")))

(register-handler
 :insert-mode-enable
 [controls-mw (after #(js/setTimeout focus-text-input! 100))]
 (fn [controls _] 
   (.addClass (js/jQuery "#controls-overlay") "insert-mode active")
   (assoc-in controls [:insert-mode?] true)))

(register-handler
 :insert-mode-disable
 controls-mw
 (fn [controls _] 
   (.removeClass (js/jQuery "#controls-overlay") "insert-mode active")
   (assoc-in controls [:insert-mode?] false)))


(register-handler
 :toggle-help
 controls-mw
 (fn [controls _]  (update-in controls [:help-visible?] #(not %))))
;; --- Channel related

(defn delete [vect item]
  (vec (remove (fn [word] (= word item)) vect)))


(register-handler 
 :delete
 channel-mw
 (fn [channels [item channel]] 
   (if (nil? channel) 
     (mapv #(assoc % :items (delete (:items %) item)) channels) ;; from all channels for now
     (mapv #(if (= % channel)
              (assoc % :items (delete (:items %) item)) 
              %) channels))))


(register-handler 
 :channel-set-mix
 channel-mw
 (fn 
   [channels [channel value]] 
   (vec (map #(if (= % channel)
                (assoc % :gain value) 
                %) channels))))

;; todo decouple this
(register-handler 
 :clear 
 channel-mw
 (fn [channels [channel]]
;   (screen/clear) ; todo separate clear handler 
   (vec (map #(if (= % channel)
                (assoc % :items [])
                %) channels))))

(register-handler
 :clear-all
 channel-mw
 (fn [channels _]
   (vec (map #(assoc % :items []) channels))))

(register-handler
 :export-all
 channel-mw
 (fn [channels _]
   (.val (js/jQuery "#textarea-export") 
         (reduce (fn [%1 %2] (str %1 
                                  (:title %2) 
                                  "\n" 
                                  (reduce #(str %1 "\t" %2 "\n") "" (:items %2)))) "" channels))
   channels))

(register-handler
 :mute
 channel-mw
 (fn [channels [channel]]
   (vec (map #(if (= % channel)
                (assoc % :muted? (not (:muted? %))) 
                %) channels))))


(register-handler
 :import
 channel-mw
 (fn [channels [text channel]]
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) (importer/process-input text)))) 
                %) channels))))

(register-handler
 :words-add
 channel-mw
 (fn [channels [words channel]]
   channels   
   (vec (map #(if (= % channel)
                (assoc % :items (vec (concat (:items %) words))) 
                %) channels))))


