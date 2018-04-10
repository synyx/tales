(ns tales.views.editor
  (:require [clojure.core.async :refer [<! go]]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.async :refer [<<<]]
            [tales.routes :refer [home-path]]
            [tales.image :as image]))

(defn- determine-image-size [project file]
  (go
    (let [dimensions (<! (<<< image/dimensions file))]
      (dispatch [:update-project (assoc project :dimensions dimensions)]))))

(defn image-upload [project]
  [:div {:id "image-upload"}
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type      "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))]
                          (dispatch [:update-project-image {:slug (:slug project) :file file}])
                          (determine-image-size project file))}]])

(defn image-size []
  [:div {:id "image-size"}
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn canvas [project]
  (let [file-path  (:file-path project)
        dimensions (:dimensions project)]
    [:img {:src file-path :style {:width (:width dimensions) :height (:height dimensions)}}]))

(defn editor-page []
  (fn []
    (let [project (subscribe [:active-project])]
      [:div {:id "editor"}
       [:header [:h1 (:name @project)]]
       [:main
        (cond
          (nil? (:file-path @project)) [image-upload @project]
          (nil? (:dimensions @project)) [image-size]
          :else [canvas @project])]
       [:footer
        [:a {:href (home-path)} "or start a new one..."]]])))
