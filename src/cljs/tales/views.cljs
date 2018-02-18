(ns tales.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [home-path editor-path]]))

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
                :value    "go tell your tale..."
                :on-click save}]])))

(defn project-list []
  (let [projects (subscribe [:projects])]
    [:ul
     (for [project @projects]
       ^{:key (:slug project)}
       [:li
        [:a {:href (editor-path {:slug (:slug project)})}
         (:name project) [:small " - " (:slug project)]]])]))

(defn project-page []
  [:div [:h2 "Welcome to tales"]
   [project-input]
   [project-list]])

(defn editor-page []
  (let [project (subscribe [:active-project])]
    [:div [:h2 "Now tell your tale..."]
     [:h1 (:name @project)]
     [:div [:a {:href (home-path)} "or start a new one..."]]]))

(defn main-page []
  (let [project (subscribe [:active-project])]
    (if-not (nil? @project)
      [editor-page]
      [project-page])))
