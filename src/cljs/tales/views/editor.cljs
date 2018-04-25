(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [home-path]]))

(defn- determine-transform [dom-dimensions image-dimensions]
  (let [transform-x (/ (:width dom-dimensions) (:width image-dimensions))
        transform-y (/ (:height dom-dimensions) (:height image-dimensions))]
    (Math/min transform-x transform-y)))

(defn image-upload [project]
  [:div {:id "image-upload"}
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type      "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))]
                          (dispatch [:update-project-image {:project project :file file}]))}]])

(defn image-size []
  [:div {:id "image-size"}
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn canvas [project _]
  (let [dom-node         (r/atom nil)
        file-path        (:file-path project)
        image-dimensions (:dimensions project)]
    (r/create-class
      {:component-did-update
       (fn [_] (.log js/console "updated editor canvas size, redraw!"))
       :component-did-mount
       (fn [this] (reset! dom-node (r/dom-node this)))
       :reagent-render
       (fn []
         [:div (if-let [node @dom-node]
                 (let [dom-dimensions {:width  (-> node .-parentNode .-clientWidth)
                                       :height (-> node .-parentNode .-clientHeight)}
                       transform      (determine-transform dom-dimensions image-dimensions)]
                   {:style {:transform (str "scale(" transform ", " transform ")")}}))
          [:img {:src   file-path
                 :style {:width  (:width image-dimensions)
                         :height (:height image-dimensions)}}]])})))

(defn editor-page []
  (fn []
    (let [project     (subscribe [:active-project])
          window-size (subscribe [:window-size])]
      [:div {:id "editor"}
       [:header [:h1 (:name @project)]]
       [:main
        (cond
          (nil? (:file-path @project)) [image-upload @project]
          (nil? (:dimensions @project)) [image-size]
          :else [canvas @project @window-size])]
       [:footer
        [:a {:href (home-path)} "or start a new one..."]]])))
