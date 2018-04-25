(ns tales.web
  (:require [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "/css/leaflet.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))
   (include-css (if (env :dev) "/css/normalize.css" "/css/normalize.min.css"))])

(defn assets []
  (list
   (include-js (if (env :dev) "/js/leaflet-src.js" "/js/leaflet.js"))
   (include-js "/js/app.js")))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     [:div#app
      [:p "Please wait..."]]
     (assets)]))

(defn cards-page []
  (html5
    (head)
    [:body
     [:div#app]
     (include-js "/js/app_devcards.js")]))
