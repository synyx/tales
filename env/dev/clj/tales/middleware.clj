(ns tales.middleware
  (:require [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-web-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-exceptions
      wrap-reload))

(defn wrap-api-middleware [handler]
  (-> handler
      wrap-json-response
      (wrap-defaults api-defaults)
      wrap-json-body
      wrap-exceptions
      wrap-reload))
