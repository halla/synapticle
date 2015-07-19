(ns app.importer)

(defn tokenize-content []
#_(js/alert "jup")

  (concat (vec (for [page js/content]                 
                 (.split page ". ")
                 ))))

(defn textarea-import [words]
  (let [ws (js->clj (.split (.-value (.getElementById js/document "textareaimport")) "\n"))]
    (println ws)
    (reset! words (js->clj ws))))
