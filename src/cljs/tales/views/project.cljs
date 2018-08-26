(ns tales.views.project
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [editor-path]]))

(defn add-project [name]
  (dispatch [:add-project {:name name}]))

(defn project-input []
  (let [val (r/atom "")
        stop #(do (reset! val ""))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (add-project v))
                (stop))]
    (fn []
      [:input {:type "text"
               :id "new-project"
               :placeholder "Enter the name of your tale and press enter"
               :value @val
               :on-change #(reset! val (-> % .-target .-value))
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

(defn page []
  (let [projects (subscribe [:projects])]
    [:div {:id "projects"}
     [:header [:h1 "tales"]]
     [:main
      [:section [project-input]]
      (if-not (empty? @projects)
        [:section
         [:h3 "or choose an existing tale:"]
         [project-list @projects]])]]))
