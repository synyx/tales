(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [home-path]]))

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
