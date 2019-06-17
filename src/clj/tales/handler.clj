(ns tales.handler
  (:require [compojure.core :refer [context defroutes routes
                                    GET POST PUT DELETE]]
            [compojure.route :refer [not-found resources files]]
            [config.core :refer [env]]
            [me.raynes.fs :as fs]
            [tales.middleware :refer [wrap-api-middleware wrap-web-middleware]]
            [tales.api :as api]
            [tales.project :refer [*project-dir*]]
            [tales.web :as web]))

(defroutes api-routes
  (wrap-api-middleware
    (routes
      (GET "/" [] (api/get-projects))
      (POST "/" {body :body} (api/create-project body))
      (context "/:slug" [slug]
        (GET "/" [] (api/get-project slug))
        (PUT "/" {body :body} (api/update-project slug body))
        (DELETE "/" [] (api/delete-project slug))
        (PUT "/image" request (api/update-project-image slug request))))))

(defroutes web-routes
  (wrap-web-middleware
    (routes
      (GET "/" [] (web/loading-page))
      (GET "/v2" [] (web/loading-page-v2))
      (context "/editor/:slug" [slug]
        (GET "/" [] (web/loading-page))
        (files "/" {:root (str (fs/file *project-dir* slug))}))
      (context "/presenter/:slug" [slug]
        (GET "/" [] (web/loading-page))
        (files "/" {:root (str (fs/file *project-dir* slug))}))
      (GET "/cards" [] (web/cards-page))
      (resources "/")
      (not-found "Not Found"))))

(defroutes app-routes
  (context "/api/tales" [] api-routes)
  web-routes)

(def app #'app-routes)
