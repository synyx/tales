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

(defn ctrl-key? [e]
  (-> e .-originalEvent .-ctrlKey))

(defn mouse-handler [map]
  (let [drawing? (subscribe [:drawing?])
        draw-start #(if (ctrl-key? %)
                      (do (-> map .-dragging .disable)
                          (dispatch [:start-draw (L/latlng-to-vec (.-latlng %))])))
        draw-end #(if @drawing? (dispatch [:end-draw (L/latlng-to-vec (.-latlng %))]))
        draw-update #(if @drawing?
                       (do (-> map .-dragging .enable)
                           (dispatch [:update-draw (L/latlng-to-vec (.-latlng %))])))]
    (-> map
      (L/on "mousedown" draw-start)
      (L/on "mouseup" draw-end)
      (L/on "mousemove" draw-update)
      (L/on "touchstart" draw-start)
      (L/on "touchend" draw-end)
      (L/on "touchmove" draw-update))))

(defn canvas [project]
  (let [slide-layer (L/layer-group)
        bounds (bounds (:dimensions project))
        map-options {:attributionControl false,
                     :zoomControl false,
                     :crs js/L.CRS.Simple,
                     :minZoom -5}]
    (r/create-class
      {:component-did-update
       (fn [this]
         (let [[_ _ slides draw-rect] (r/argv this)]
           (L/clear-layers slide-layer)
           (if draw-rect
             (L/add-layer slide-layer draw-rect))
           (if-not (empty? slides)
             (doseq [slide slides]
               (L/add-layer slide-layer (L/rectangle slide))))))
       :component-did-mount
       (fn [this]
         (-> (L/map (r/dom-node this) map-options)
           (L/add-layer (L/image-overlay (:file-path project) bounds))
           (L/add-layer slide-layer)
           (L/fit-bounds bounds)
           mouse-handler))
       :reagent-render
       (fn [] [:div])})))

(defn canvas-container []
  (let [project (subscribe [:active-project])
        slides (subscribe [:slides])
        draw-rect (subscribe [:draw-rect])]
    (fn []
      [canvas @project @slides @draw-rect])))

(defn editor-page []
  (let [project (subscribe [:active-project])]
    (fn []
      [:div {:id "editor"}
       [:header [:h1 (:name @project)]]
       [:main
        (cond
          (nil? (:file-path @project)) [image-upload @project]
          (nil? (:dimensions @project)) [image-size]
          :else [canvas-container])]
       [:footer
        [:a {:href (home-path)} "or start a new one..."]]])))
