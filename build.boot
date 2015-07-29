(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"html"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2814-4" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT" :scope "test"]
                 [adzerk/boot-reload    "0.3.1"      :scope "test"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.5.0"]
                 [domina "1.0.3"]
                 [dragonmark/web "0.1.7"]
                 [cljsjs/jquery "1.9.1-0"]
                 [cljsjs/jquery-ui "1.11.3-1"]
                 [boot-cljs-test/node-runner "0.1.0" :scope "test"]
                 [org.clojure/clojurescript "0.0-3165" ]
                 [pandeiro/boot-http    "0.3.0"      :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[boot-cljs-test.node-runner :refer :all]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.http         :refer [serve]])

(require 'boot.repl)
(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.9.0"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)

(swap! boot.repl/*default-dependencies* conj
       '[refactor-nrepl "1.1.0-SNAPSHOT"])

(swap! boot.repl/*default-middleware* conj
       'refactor-nrepl.middleware/wrap-refactor)


(deftask dev []
  (set-env! 
   :source-paths #{"src" "test"})
  (comp (serve :dir "target/")
        (watch)
        (speak)
        (reload :on-jsload 'app.core/main)
        (cljs-repl)
#_        (cljs-test-node-runner :namespaces '[app.test])
        (cljs :source-map true :optimizations :none)
  #_      (run-cljs-test)))

(deftask build []
  (set-env! :source-paths #{"src"})
  (comp (cljs :optimizations :advanced)))


