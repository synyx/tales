(defproject tales "_"
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
                 [venantius/accountant "0.2.4"]]

  :plugins [[lein-environ "1.1.0"]
            [me.arrdem/lein-git-version "2.0.8"]]

  :git-version {:status-to-version
                (fn [{:keys [tag ahead? dirty?] as :git}]
                  (assert (re-find #"\d+\.\d+\.\d+" tag)
                          "Tag is assumed to be in SemVer format")
                  (if (and tag (not ahead?) (not dirty?))
                    tag
                    (let [[_ prefix patch] (re-find #"(\d+\.\d+)\.(\d+)" tag)
                          patch            (Long/parseLong patch)
                          patch+           (inc patch)]
                      (format "%s.%d-SNAPSHOT" prefix patch+))))}

  :ring {:handler tales.handler/app
         :uberwar-name "tales.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "tales.jar"

  :main tales.server

  :clean-targets ^{:protect false} [:target-path
                                    [:builds :app :compiler :output-dir]
                                    [:builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["spec/clj" "test/clj"]
  :resource-paths ["resources"]

  :doo {:build "test"
        :alias {:default [:chrome]}}

  :profiles {:uberjar {:source-paths ["env/prod/clj"]
                       :prep-tasks ["compile"]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
