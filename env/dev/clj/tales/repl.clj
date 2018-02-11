(ns tales.repl
  (:use figwheel-sidecar.repl-api)
  (:require [tales.handler :refer [app]]
            [ring.server.standalone :refer [serve]]
            [ring.middleware
             [file :refer [wrap-file]]
             [content-type :refer [wrap-content-type]]
             [not-modified :refer [wrap-not-modified]]]))

(defonce server (atom nil))

(defn get-handler []
  (-> #'app
      (wrap-file "resources")
      wrap-content-type
      wrap-not-modified))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port         port
                    :auto-reload? true
                    :join?        false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop @server)
  (reset! server nil))
