(ns tales.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn add-project [name]
  (dispatch [:add-project {:name name}]))

(defn project-input []
  (let [val  (r/atom "")
        stop #(do (reset! val ""))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (add-project v))
                (stop))]
    (fn []
      [:div
       [:input {:type        "text"
                :value       @val
                :on-change   #(reset! val (-> % .-target .-value))
                :on-key-down #(case (.-which %)
                                13 (save)
                                27 (stop)
                                nil)}]
       [:input {:type     "button"
                :value    "Create"
                :on-click save}]])))

(defn projects-list []
  (let [projects (subscribe [:projects])]
    [:ul
     (for [project @projects]
       ^{:key (:slug project)} [:li (:name project) [:small " - " (:slug project)]])]))

(defn project-page []
  [:div [:h2 "Welcome to tales"]
   [project-input]
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
