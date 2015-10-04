(ns app.view.exports
  (:require [re-com.core :refer [h-box v-box box gap line label
                                 button
                                 input-textarea modal-panel
                                 popover-content-wrapper popover-anchor-wrapper]]
            [re-com.util :refer [deref-or-value]]
            [re-frame.core :refer [dispatch-sync subscribe]]
            [reagent.core :as reagent :refer [atom]]))


(defn channels->string 
  "All items to tab-indented plain string list"
  [channels]
  (reduce (fn [%1 %2] (str %1 
                           (:title %2) 
                           "\n" 
                           (reduce #(str %1 "\t" %2 "\n") 
                                   "" 
                                   (:items %2)))) 
          "" 
          channels))

(defn export-component-body-func 
  [submit-dialog cancel-dialog dialog-data]  
  (fn []
    [v-box
     :children [[label
                 :class "help-text"
                 :label "Copy paste the tab-indented plain string to your destination of choice."]
                [gap :size "15px"]
                (with-meta ;; TODO focus doesn't work
                  [input-textarea
                   :model            dialog-data
                   :width            "100%"
                   :rows             10
                   :on-change        #(reset! dialog-data %)
                   :change-on-blur?  true]
                  {:component-did-mount #(do (.focus (reagent/dom-node %))
                                             (.select (reagent/dom-node %)))})
                [gap :size "20px"]
                [line]
                [gap :size "10px"]
                [h-box
                 :gap      "10px"
                 :children [[button
                             :label    [:span [:i {:class "zmdi zmdi-check" }] "OK"]
                             :on-click #(submit-dialog @dialog-data)
                             :class    "btn-primary"]]]]]))

(defn popover-body-export
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
       :title            "Export all items"
       :body             [(export-component-body-func submit-dialog cancel-dialog dialog-data)]])))


(defn export-dlg 
  "Export all items from all channels, as tab-indented plain string list"
  [channels]
  (let [showing? (atom false)
        on-change #()]
    (fn [channels]
      [popover-anchor-wrapper
       :showing? showing?
       :position :right-below
       :anchor [button 
                :label "Export"
                :class "btn-default btn-sm"
                :tooltip "Export items from all channels as plain text list"
                :on-click #(reset! showing? true)]
       :popover [popover-body-export
                 showing? 
                 :right-below 
                 (channels->string @channels) 
                 on-change]])))
