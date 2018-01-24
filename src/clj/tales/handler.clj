(ns tales.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [tales.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     [:div#app
      [:p "Please wait..."]]
     (include-js "/js/app.js")]))

(defn cards-page []
  (html5
    (head)
    [:body
     [:div#app]
     (include-js "/js/app_devcards.js")]))

(defroutes routes
           (GET "/" [] (loading-page))
           (if (env :dev) (GET "/cards" [] (cards-page)))
           (resources "/")
           (not-found "Not Found"))

(def app (wrap-middleware #'routes))
