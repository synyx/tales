(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [home-path]]
            [tales.leaflet.core :as L]))

(defn- bounds [image-dimensions]
  [[0 0] [(:height image-dimensions) (:width image-dimensions)]])

(defn image-upload [project]
  [:div {:id "image-upload"}
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))
                              data {:project project :file file}]
                          (dispatch [:update-project-image data]))}]])

(defn image-size []
  [:div {:id "image-size"}
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn canvas [project]
  (let [bounds (bounds (:dimensions project))
        map-options {:attributionControl false,
                     :zoomControl false,
                     :crs js/L.CRS.Simple,
                     :minZoom -5}]
    (r/create-class
      {:component-did-update
       (fn [_] (.log js/console "updated editor canvas size, redraw!"))
       :component-did-mount
       (fn [this]
         (-> (L/map (r/dom-node this) map-options)
           (L/add-layer (L/image-overlay (:file-path project) bounds))
           (L/fit-bounds bounds)))
       :reagent-render
       (fn [] [:div])})))

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
