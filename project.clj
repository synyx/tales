(defproject tales "0.1.0-SNAPSHOT"
  :description "synyx Tales"
  :url "https://github.com/synyx/tales"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [ring-server "0.5.0"]
                 [ring "1.7.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [me.raynes/fs "1.4.6"]
                 [yogthos/config "1.1.1"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.1"]
                 [re-frame "0.10.6"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4"]]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.2.7" :exclusions [org.clojure/clojure]]]

  :ring {:handler tales.handler/app
         :uberwar-name "tales.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "tales.jar"

  :main tales.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["spec/clj" "test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:min {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
                             :compiler {:output-to "target/cljsbuild/public/js/app.js"
                                        :output-dir "target/cljsbuild/public/js"
                                        :source-map "target/cljsbuild/public/js/app.js.map"
                                        :optimizations :advanced
                                        :pretty-print false}}
                       :app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                             :figwheel {:on-jsload "tales.core/mount-root"}
                             :compiler {:main "tales.dev"
                                        :asset-path "/js/out"
                                        :output-to "target/cljsbuild/public/js/app.js"
                                        :output-dir "target/cljsbuild/public/js/out"
                                        :source-map true
                                        :optimizations :none
                                        :pretty-print true}}
                       :test {:source-paths ["src/cljs" "src/cljc" "test/cljs"]
                              :compiler {:main tales.doo-runner
                                         :asset-path "/js/out"
                                         :output-to "target/test.js"
                                         :output-dir "target/cljstest/public/js/out"
                                         :optimizations :none
                                         :pretty-print true}}
                       :devcards {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                                  :figwheel {:devcards true}
                                  :compiler {:main "tales.cards"
                                             :asset-path "js/devcards_out"
                                             :output-to "target/cljsbuild/public/js/app_devcards.js"
                                             :output-dir "target/cljsbuild/public/js/devcards_out"
                                             :source-map-timestamp true
                                             :optimizations :none
                                             :pretty-print true}}}}

  :doo {:build "test"
        :alias  {:default  [:chrome]}}

  :figwheel {:http-server-root "public"
             :server-logfile false
             :server-port 3449
             :nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/public/css"]
             :ring-handler tales.handler/app}



  :profiles {:dev {:repl-options {:init-ns tales.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :dependencies [[binaryage/devtools "0.9.10"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.7.0"]
                                  [prone "1.6.0"]
                                  [figwheel-sidecar "0.5.16"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [speclj "3.3.2"]
                                  [devcards "0.2.6" :exclusions [cljsjs/react]]
                                  [pjstadig/humane-test-output "0.8.3"]]
                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.16"]
                             [lein-doo "0.1.10"]]
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]
                   :env {:dev true}}
             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
