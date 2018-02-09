(ns tales.middleware
  (:require [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defn wrap-web-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)))

(defn wrap-api-middleware [handler]
  (-> handler
      wrap-json-response
      (wrap-defaults api-defaults)
      wrap-json-body))

(defn wrap-multipart-params-middleware [handler]
  (-> handler
      wrap-params
      wrap-multipart-params
      (wrap-defaults api-defaults)))
