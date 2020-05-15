(ns tales.web
  (:require [config.core :refer [env]]
            [hiccup.page :refer [include-js include-css html5]]))

(defn loading-page []
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
