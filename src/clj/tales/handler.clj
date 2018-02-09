(ns tales.handler
  (:require [compojure.core :refer [context defroutes routes GET POST PUT DELETE]]
            [compojure.route :refer [not-found resources]]
            [config.core :refer [env]]
            [tales.middleware :refer [wrap-api-middleware wrap-web-middleware]]
            [tales.api :as api]
            [tales.web :as web]))

(defroutes api-routes
    (wrap-api-middleware
      (routes
        (GET "/" [] (api/find-all))
        (POST "/" {body :body} (api/create body))
        (context "/:slug" [slug]
          (GET "/" [] (api/find-by-slug slug))
          (PUT "/" {body :body} (api/update slug body))
          (DELETE "/" [] (api/delete slug))))))

(defroutes web-routes
  (wrap-web-middleware
    (routes
      (GET "/" [] (web/loading-page))
      (if (env :dev) (GET "/cards" [] (web/cards-page)))
      (resources "/")
      (not-found "Not Found"))))

(defroutes app-routes
           (context "/api/tales" [] api-routes)
           web-routes)

(def app #'app-routes)
