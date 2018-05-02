(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :refer [home-path]]
            [tales.leaflet.core :as L]
            [tales.leaflet.helper :as L.helper]))

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

(defn slide-preview-item [project slide width height]
  (let [slide (-> slide
                L.helper/normalize-bounds
                L.helper/bounds->map)
        slide-width (- (:x2 slide) (:x1 slide))
        slide-height (- (:y2 slide) (:y1 slide))
        scale (if (> (/ width height) (/ slide-width slide-height))
                (/ height slide-height)
                (/ width slide-width))
        div-width (* scale slide-width)
        div-height (* scale slide-height)
        dx (* scale (:x1 slide))
        dy (* scale (- (:height (:dimensions project)) (:y2 slide)))
        scaled-img-width (* scale (:width (:dimensions project)))
        scaled-img-height (* scale (:height (:dimensions project)))]
    (fn []
      [:div {:style {:width div-width
                     :height div-height
                     :background-color "#fff"
                     :background-repeat "no-repeat"
                     :background-image (str "url(" (:file-path project) ")")
                     :background-size (str scaled-img-width "px " scaled-img-height "px")
                     :background-position (str (- dx) "px " (- dy) "px")}}])))

(defn slide-preview [project]
  (let [slides (subscribe [:slides])
        current-slide @(subscribe [:current-slide])
        preview-width 100
        preview-height 75]
    [:div {:id "slide-preview" :class "slide-preview-list"}
     (map-indexed (fn [idx slide]
                    ^{:key idx}
                    [:div {:class "slide-preview-list-item"
                           :style {:width preview-width :min-width preview-width
                                   :height preview-height :min-height preview-height
                                   :background-color "#333"
                                   :border-width 3
                                   :border-style "solid"
                                   :border-color (if (= idx current-slide) "#ff0000" "#333")}
                           :on-click #(dispatch [:activate-slide idx])
                           :on-double-click #(dispatch [:move-to-slide idx])}
                     [slide-preview-item project slide preview-width preview-height]]) @slides)]))

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

(defn navigator [project]
  (let [map (r/atom nil)
        slide-layer (L/layer-group)
        bounds (L.helper/bounds (:dimensions project))
        map-options {:attributionControl false
                     :zoomControl false
                     :crs js/L.CRS.Simple
                     :minZoom -5
                     :zoomSnap 0}
        update (fn [this]
                 (let [[_ _ slides current-slide draw-rect] (r/argv this)]
                   (L/clear-layers slide-layer)
                   (if draw-rect
                     (L/add-layer slide-layer draw-rect))
                   (if-not (empty? slides)
                     (doall (map-indexed (fn [idx slide]
                                           (let [color (if (= idx current-slide) "#ff0000" "#3388ff")]
                                             (L/add-layer slide-layer
                                               (L/rectangle slide {:color color})))) slides)))))]
    (r/create-class
      {:component-did-update update
       :component-did-mount
       (fn [this]
         (reset! map (L/map (r/dom-node this) map-options))
         (-> @map
           (L/add-layer (L/image-overlay (:file-path project) bounds))
           (L/add-layer slide-layer)
           (L/fit-bounds bounds)
           mouse-handler)
         (dispatch [:navigator-available @map])
         (update this))
       :component-will-unmount (fn [] (dispatch [:navigator-unavailable @map]))
       :reagent-render (fn [] [:div {:id "navigator"}])})))

(defn navigator-container [project]
  (let [slides (subscribe [:slides])
        current-slide (subscribe [:current-slide])
        draw-rect (subscribe [:draw-rect])]
    (fn []
      [navigator project @slides @current-slide @draw-rect])))

(defn editor-page []
  (let [project (subscribe [:active-project])]
    (fn []
      [:div {:id "editor"}
       [:header
        [:h1 (:name @project)]
        [:a {:href (home-path)} "Close"]]
       [:main (cond
                (nil? (:file-path @project)) [image-upload @project]
                (nil? (:dimensions @project)) [image-size]
                :else [navigator-container @project])]
       [:footer [slide-preview @project]]])))
