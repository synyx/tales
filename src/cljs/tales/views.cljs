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
      [:input {:type        "text"
               :id          "new-project"
               :placeholder "Enter the name of your tale and press enter"
               :value       @val
               :on-change   #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(defn project-list [projects]
  [:ul {:id "project-list"}
   (for [[_ project] projects]
     ^{:key (:slug project)}
     [:li
      [:a {:href (editor-path {:slug (:slug project)})}
       (:name project)]])])

(defn project-page []
  (let [projects (subscribe [:projects])]
    [:div {:id "projects"}
     [:header [:h1 "tales"]]
     [:main
      [:section [project-input]]
      (if-not (empty? @projects)
        [:section [:h3 "or choose an existing tale:"] [project-list @projects]])]]))

(defn editor-upload [project]
  [:div {:id "editor-upload"}
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type "file"
            :on-change
                  #(dispatch
                     [:update-project-image
                      {:slug (:slug project)
                       :file (-> % .-target .-files (aget 0))}])}]])

(defn editor-canvas [project]
  (let [file-path (:file-path project)]
    [:img {:src file-path}]))

(defn editor-page []
  (let [project (subscribe [:active-project])]
    [:div {:id "editor"}
     [:header [:h1 (:name @project)]]
     [:main
      (if (nil? (:file-path @project))
        [editor-upload @project]
        [editor-canvas @project])]
     [:footer
      [:a {:href (home-path)} "or start a new one..."]]]))

(defn main-page []
  (let [project (subscribe [:active-project])]
    (if-not (nil? @project)
      [editor-page]
      [project-page])))
