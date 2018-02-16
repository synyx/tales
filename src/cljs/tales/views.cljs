(ns tales.views
  (:require [re-frame.core :refer [subscribe]]))

(defn editor-page []
  [:div [:h2 "Welcome to tales"]
   [:div [:a {:href "#tell"} "go tell your tale"]]])

(defn tell-page []
  [:div [:h2 "Now tell your tale..."]
   [:div [:a {:href "#"} "go to the editor"]]])

(defn- pages [page-name]
  (case page-name
    :editor-page [editor-page]
    :tell-page [tell-page]
    [:div]))

(defn main-page []
  (let [active-page (subscribe [:active-page])]
    [:div [pages @active-page]]))
