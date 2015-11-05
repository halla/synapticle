(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"html"}
 :dependencies '[[adzerk/boot-cljs      "1.7.166-1" :scope "test"]
                 [adzerk/boot-cljs-repl "0.2.0" :scope "test"]
                 [adzerk/boot-reload    "0.4.1"      :scope "test"]

                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/schema "1.0.3"]
                 [reagent "0.5.1"]
                 [re-frame "0.5.0"]
                 [re-com "0.6.2"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 [dragonmark/web "0.1.8"]
                 [markdown-clj "0.9.71"]
                 [cljsjs/jquery "1.9.1-0"]
                 [cljsjs/jquery-ui "1.11.3-1"]
                 [cljsjs/mousetrap "1.5.3-0"]
                 [org.clojure/clojurescript "1.7.145" ]
                 [boot-cljs-test/node-runner "0.1.0" :scope "test"]
                 [pandeiro/boot-http    "0.6.3"      :scope "test"]
                 [org.clojure/test.check "0.8.2" :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl repl-env]]
 '[boot-cljs-test.node-runner :refer :all]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http         :refer [serve]])

(require 'boot.repl)
(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.9.1"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)

(swap! boot.repl/*default-dependencies* conj
       '[refactor-nrepl "1.1.0"])

(swap! boot.repl/*default-middleware* conj
       'refactor-nrepl.middleware/wrap-refactor)


(deftask dev []
  (set-env! 
   :source-paths #{"src"})
  (comp (serve :dir "target/" :port 3456)
        (watch)
        (speak)
        (reload :on-jsload 'app.core/main)
        (cljs-repl)
        (cljs 
              :source-map true 
              :optimizations :none)))

(deftask test []
  (set-env! 
   :source-paths #{"src" "test"})

  (comp (serve :dir "target/")
        (watch)
        (speak)
        (reload)
        (cljs-repl)
#_        (cljs-test-node-runner :namespaces '[app.misc-test])
        (cljs :source-map true 
              :optimizations :none)
 #_       (run-cljs-test)))

(deftask prod []
  (set-env! :source-paths #{"src"})
  (comp (serve :dir "target/")
        (watch)
        (reload :on-jsload 'app.core/main)
        (cljs-repl)
        (cljs :optimizations :advanced)))

(deftask build []
  (set-env! :source-paths #{"src"})
  (comp  (cljs :optimizations :advanced)))


