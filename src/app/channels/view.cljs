(ns app.channels.view
  (:require [re-frame.core :refer [dispatch-sync
                                   subscribe
                                   ]]
            [dragonmark.web.core :as dw :refer [xform]]
            [app.imports.view :as imports]
            [app.view.exports :as exports]
            [re-com.core  :refer [h-box v-box box gap line label checkbox 
                                  radio-button button single-dropdown
                                  input-textarea modal-panel slider
                                  popover-content-wrapper popover-anchor-wrapper]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [app.templates :refer [deftmpl]]
                   [reagent.ratom :refer [reaction]]))


(deftmpl dataview-tpl "dataview.html")

(defn allow-drop [e]
  (.preventDefault e))


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


(defn channel-item 
  "Single item in single channel. Editable, deletable."
 [item channel]
  (let [editing (atom false)]
    (fn [item channel]  
      [:div {:class "channel-item"}  
       (if @editing
         [title-edit {:title item
                      :on-save #(dispatch-sync [:channel-update-item @channel item %])
                      :on-stop #(reset! editing false)}]
         [:span {:on-click #(reset! editing (not @editing))
                 :class "text"} item]) 
       [:button {:class "btn btn-xs"
                 :on-click #(dispatch-sync [:delete item @channel])}

        [:a {:class "delete"}  "\u274c" ] ]])))

(defn channel-items [items channel]
  [:ul {:class "list-unstyled channel"} 
   (for [item items] 
     [channel-item item channel])])

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

(defn mix-slider [channel]  
  [slider
   :model     (reaction  (:gain @channel))
   :min       0
   :max       1.0
   :step      0.1
   :class "form-control"
   :width     "100px"
   :on-change #(do (dispatch-sync [:channel-set-mix @channel (float %)])
                   (dispatch-sync [:start]))
   :disabled? false])

(defn channel-controls [channel]
  [:ul {:class "list-unstyled"}
   [:li [mix-slider channel] ]
   [:li {:class "list-unstyled"} [imports/import-dlg channel]]
   [:li {:class "list-unstyled"}
    [button 
         :label "Clear"
         :class "btn-default btn-sm"
         :disabled? false
         :on-click #(dispatch-sync [:clear @channel])
         :tooltip "Remove all items from this channel"]]])

(defn channel-view [channels idx]
  (let [channel (reaction (@channels idx))]
    (fn [channels idx]
      [:li {:class "channel list-unstyled"}
       [:div {:class "channel-title"} (:title @channel)]
       [:div {:class "channel-controls"} [channel-controls channel]]
       [:div {:class "channel-items"} (channel-items (:items @channel) channel)]])))

(defn dataview [active-channel channels active-list-idx]
  "Editing channels and data"
  (xform dataview-tpl
         [".datalist" {:on-drag-over allow-drop
                       :on-drag-enter allow-drop
                       :on-drop (fn [e]
                                  (.preventDefault e)
                                  (let [tree (.getData (.-dataTransfer e) "text")]
                                    (dispatch-sync [:import tree @active-channel])))}]
         [".datalist" :* [channel-items (:items @active-channel) active-channel] ]
         [".nav-tabs li" :* (data-tab-item channels @active-list-idx) ]
         ["#channels" :*> (for [idx (range (count @channels))] 
                            [channel-view channels idx])]
         [".channels.buttons" :*> (list 
                                  [:li [exports/export-dlg channels]]
                                  [:li [button 
                                     :label "Clear all"
                                     :class "btn-default btn-sm"
                                     :on-click #(dispatch-sync [:clear-all])
                                     :tooltip "Remove all items from all channels"]])]
         ["#channel-controls .channel-mix"  
          {:value (:gain (@channels @active-list-idx))
           :on-change #(dispatch-sync 
                        [:channel-set-mix 
                         (@channels @active-list-idx) 
                         (cljs.reader/read-string (.. % -target -value))])}]
         ["#channel-controls" :*> (list [:li [imports/import-dlg active-channel]]
                                        [:li [button 
                                     :label "Clear"
                                     :class "btn-default btn-sm"
                                     :disabled? false
                                     :on-click #(dispatch-sync [:clear @active-channel])
                                     :tooltip "Remove all items from this channel"]]
                               )]))
