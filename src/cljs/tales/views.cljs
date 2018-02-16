(ns tales.views
  (:require [re-frame.core :refer [subscribe]]))

(defn projects-list []
  (let [projects (subscribe [:projects])]
    [:ul
     (for [project @projects]
       ^{:key (:slug project)} [:li (:name project)])]))

(defn project-page []
  [:div [:h2 "Welcome to tales"]
   [projects-list]
   [:div [:a {:href "#editor"} "go tell your tale..."]]])

(defn editor-page []
  [:div [:h2 "Now tell your tale..."]
   [:div [:a {:href "#"} "or start a new one..."]]])

(defn- pages [page-name]
  (case page-name
    :project-page [project-page]
    :editor-page [editor-page]
    [:div]))

(defn main-page []
  (let [active-page (subscribe [:active-page])]
    [:div [pages @active-page]]))
