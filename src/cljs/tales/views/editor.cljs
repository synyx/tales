(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :as routes]
            [tales.leaflet.core :as L]
            [tales.views.preview :as preview]
            [tales.views.slide :as slide]))

(defn image-upload [project]
  [:div#image-upload
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))
                              data {:project project :file file}]
                          (dispatch [:update-project-image data]))}]])

(defn image-size []
  [:div#image-size
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn ctrl-key? [e]
  (-> e .-originalEvent .-ctrlKey))

(defn draw-handler [map]
  (let [drawing? (subscribe [:drawing?])
        draw-start #(if (ctrl-key? %)
                      (do (-> map .-dragging .disable)
                          (dispatch [:start-draw (.-latlng %)])))
        draw-end #(if @drawing?
                    (dispatch [:end-draw (.-latlng %)]))
        draw-update #(if @drawing?
                       (do (-> map .-dragging .enable)
                           (dispatch [:update-draw (.-latlng %)])))]
    (-> map
      (L/on "mousedown" draw-start)
      (L/on "mouseup" draw-end)
      (L/on "mousemove" draw-update)
      (L/on "touchstart" draw-start)
      (L/on "touchend" draw-end)
      (L/on "touchmove" draw-update))))

(defn slide-layer [layer-container]
  (let [slides (subscribe [:slides])
        current-slide (subscribe [:current-slide])
        layer (r/atom nil)
        will-mount (fn []
                     (reset! layer (L/create-feature-group)))
        did-mount (fn []
                    (L/add-layer layer-container @layer))
        will-unmount (fn []
                       (L/remove-layer layer-container @layer))
        render (fn []
                 (let [layer @layer
                       current-slide @current-slide]
                   [:div {:style {:display "none"}}
                    (for [slide @slides]
                      (let [current? (= (:index slide) current-slide)
                            color (if current? "#ff9900" "#3388ff")]
                        ^{:key (:index slide)} [slide/rect {:color color} layer slide]))]))]
    (r/create-class
      {:display-name "slide-layer"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn draw-layer [layer-container]
  (let [draw-slide (subscribe [:draw-slide])
        layer (r/atom nil)
        will-mount (fn []
                     (reset! layer (L/create-feature-group)))
        did-mount (fn []
                    (L/add-layer layer-container @layer))
        will-unmount (fn []
                       (L/remove-layer layer-container @layer))
        render (fn []
                 [:div {:style {:display "none"}}
                  (if @draw-slide
                    [slide/rect {:color "#ff9900"} @layer @draw-slide])])]
    (r/create-class
      {:display-name "draw-layer"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn navigator [project]
  (let [leaflet-map (r/atom nil)
        bounds [[0 0]
                [(:height (:dimensions project))
                 (:width (:dimensions project))]]
        map-options {:attributionControl false
                     :zoomControl false
                     :crs js/L.CRS.Simple
                     :minZoom -5
                     :zoomSnap 0}
        did-mount (fn [this]
                    (reset! leaflet-map
                      (L/create-map (r/dom-node this) map-options))
                    (-> @leaflet-map
                      (L/add-layer
                        (L/create-image-overlay (:file-path project) bounds))
                      (L/fit-bounds bounds)
                      draw-handler)
                    (dispatch [:navigator-available @leaflet-map])
                    (r/force-update this))
        will-unmount (fn []
                       (dispatch [:navigator-unavailable @leaflet-map]))
        render (fn []
                 (let [layer-container @leaflet-map]
                   (if @leaflet-map
                     [:div#navigator
                      [slide-layer layer-container]
                      [draw-layer layer-container]]
                     [:div#navigator])))]
    (r/create-class
      {:display-name "navigator"
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn page []
  (let [project (subscribe [:active-project])]
    (fn []
      [:div {:id "editor"}
       [:header
        [:h1 (:name @project)]
        [:a {:href (routes/home-path)} "Close"]]
       [:main (cond
                (nil? (:file-path @project)) [image-upload @project]
                (nil? (:dimensions @project)) [image-size]
                :else [navigator @project])]
       [:footer [preview/slides @project]]])))
