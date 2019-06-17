(ns tales.web
  (:require [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "/vendor/normalize/normalize.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn assets []
  (include-js "/js/app.js"))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     [:div#app
      [:p "Please wait..."]]
     (assets)]))

(defn loading-page-v2 []
  (html5
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]
        (include-css "/vendor/normalize/normalize.css")
        (include-css "/css/tales.css")]
    [:body {:class "body-container"}
     [:div#app]
     (include-js "/js/tales.js")
     [:script "tales.init();"]]))

(defn cards-page []
  (html5
    (head)
    [:body
     [:div#app]
     (include-js "/js/app_devcards.js")]))
