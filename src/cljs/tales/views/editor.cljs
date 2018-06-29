(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :as routes]
            [tales.leaflet.core :as L]
            [tales.views.preview :as preview]))

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

(defn corners [bounds]
  {:north-west (.getNorthWest bounds)
   :north-east (.getNorthEast bounds)
   :south-east (.getSouthEast bounds)
   :south-west (.getSouthWest bounds)})

(defn opposite-corner [corner]
  (case corner
    :north-west :south-east
    :north-east :south-west
    :south-east :north-west
    :south-west :north-east
    nil))

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

(defn slide-rect [props layer-container slide]
  (let [rectangle (r/atom nil)
        on-click #(do (dispatch [:activate-slide (:index slide)])
                      (.stopPropagation js/L.DomEvent %))
        on-dblclick #(do (dispatch [:move-to-slide (:index slide)])
                         (.stopPropagation js/L.DomEvent %))
        will-mount (fn []
                     (let [bounds (L/slide-rect->latlng-bounds (:rect slide))]
                       (reset! rectangle (L/create-rectangle bounds))
                       (-> @rectangle
                         (L/set-style props)
                         (L/on "click" on-click)
                         (L/on "dblclick" on-dblclick))))
        did-mount (fn []
                    (L/add-layer layer-container @rectangle))
        did-update (fn [this [_ prev-props _ prev-slide]]
                     (let [[_ _ _ slide] (r/argv this)]
                       (if-not (= prev-props (r/props this))
                         (L/set-style @rectangle (r/props this)))
                       (if-not (= prev-slide slide)
                         (L/set-bounds @rectangle
                           (L/slide-rect->latlng-bounds (:rect slide))))))
        will-unmount (fn []
                       (-> @rectangle
                         (L/off "click")
                         (L/off "dblclick"))
                       (L/remove-layer layer-container @rectangle))
        render (fn [] [:div {:style {:display "none"}}])]
    (r/create-class
      {:display-name "navigator-slide"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn edit-rect [props layer-container slide]
  (let [rectangle (r/atom nil)
        markers (r/atom [])
        start-draw (fn [rectangle corner e]
                     (let [opposite ((opposite-corner corner) (corners (.getBounds rectangle)))]
                       (dispatch [:start-draw opposite (.-latlng e) true])))
        icon (.divIcon js/L (clj->js {:className "slide-resize-marker"}))
        will-mount (fn []
                     (let [bounds (L/slide-rect->latlng-bounds (:rect slide))]
                       (reset! rectangle (L/create-rectangle bounds))
                       (-> @rectangle
                         (L/set-style props))
                       (doseq [[corner latlng] (corners bounds)]
                         (let [options {:icon icon :draggable true}
                               marker (.marker js/L (clj->js latlng) (clj->js options))]
                           (reset! markers (conj @markers marker))
                           (L/on marker "mousedown" #(start-draw @rectangle corner %))
                           (L/on marker "touchstart" #(start-draw @rectangle corner %))))))
        did-mount (fn []
                    (L/add-layer layer-container @rectangle)
                    (doseq [marker @markers]
                      (L/add-layer layer-container marker)))
        did-update (fn [this [_ prev-props _ prev-slide]]
                     (let [[_ _ _ slide] (r/argv this)]
                       (if-not (= prev-props (r/props this))
                         (L/set-style @rectangle (r/props this)))
                       (if-not (= prev-slide slide)
                         (let [bounds (L/slide-rect->latlng-bounds (:rect slide))]
                           (L/set-bounds @rectangle bounds)
                           (doseq [[marker [_ latlng]] (map vector @markers (corners bounds))]
                             (.setLatLng marker (clj->js latlng)))))))
        will-unmount (fn []
                       (doseq [marker @markers]
                         (L/off marker "mousedown")
                         (L/off marker "touchstart")
                         (L/remove-layer layer-container marker))
                       (L/remove-layer layer-container @rectangle))
        render (fn [] [:div {:style {:display "none"}}])]
    (r/create-class
      {:display-name "navigator-slide"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))

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
                        (if current?
                          ^{:key (:index slide)} [edit-rect {:color color} layer slide]
                          ^{:key (:index slide)} [slide-rect {:color color} layer slide])))]))]
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
                    [slide-rect {:color "#ff9900"} @layer @draw-slide])])]
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
