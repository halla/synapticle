(ns app.imports
 (:require [re-com.core :refer [h-box v-box box gap line label
                                 button
                                 input-textarea modal-panel
                                 popover-content-wrapper popover-anchor-wrapper]]
            [re-com.util :refer [deref-or-value]]
            [re-frame.core :refer [dispatch-sync subscribe]]
            [reagent.core :as reagent :refer [atom]]))

(defn import-component-body-func 
  [submit-dialog cancel-dialog dialog-data]  
  (fn []
    [v-box
     :children [[label
                 :class "help-text"
                 :label "Type (or paste) text into the text area (one item per line) and hit import."]
                [gap :size "15px"]
                [input-textarea
                 :model            dialog-data
                 :width            "100%"
                 :rows             10
                 :placeholder      "Enter items, one per line"
                 :on-change        #(reset! dialog-data %)
                 :change-on-blur?  true]
                [gap :size "20px"]
                [line]
                [gap :size "10px"]
                [h-box
                 :gap      "10px"
                 :children [[button
                             :label    [:span [:i {:class "zmdi zmdi-check" }] "Import"]
                             :on-click #(submit-dialog @dialog-data)
                             :class    "btn-primary"]]]]]))



(defn popover-body-import
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
                :class "btn-default btn-sm"
                :on-click #(reset! showing? true)]
       :popover [popover-body-import showing? :right-below dlg-data on-change]])))
