(ns tales.views.slide
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.leaflet.core :as L]))

(defn corners [bounds]
  "Lists all corners of bounds with direction"
  {:north-west (.getNorthWest bounds)
   :north-east (.getNorthEast bounds)
   :south-east (.getSouthEast bounds)
   :south-west (.getSouthWest bounds)})

(defn opposite-corner [corner]
  "Names the opposite of a corner"
  (case corner
    :north-west :south-east
    :north-east :south-west
    :south-east :north-west
    :south-west :north-east
    nil))

(defn rect [props layer-container slide]
  (let [rectangle (r/atom nil)
        markers (r/atom [])

        icon (.divIcon js/L (clj->js {:className "slide-resize-marker"}))

        start-draw (fn [rectangle corner e]
                     (let [opposite ((opposite-corner corner) (corners (.getBounds rectangle)))]
                       (dispatch [:start-draw opposite (.-latlng e) true])))

        on-click #(do (dispatch [:activate-slide (:index slide)])
                      (.stopPropagation js/L.DomEvent %))

        on-dblclick #(do (dispatch [:move-to-slide (:index slide)])
                         (.stopPropagation js/L.DomEvent %))

        will-mount (fn []
                     (let [bounds (L/slide-rect->latlng-bounds (:rect slide))
                           active? (:active? props)
                           color (if active? "#ff9900" "#3388ff")]
                       (reset! rectangle (L/create-rectangle bounds))
                       (-> @rectangle
                         (L/set-style {:color color})
                         (L/on "click" on-click)
                         (L/on "dblclick" on-dblclick))
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
                         (let [active? (:active? (r/props this))
                               color (if active? "#ff9900" "#3388ff")
                               display (if active? "block" "none")]
                           (L/set-style @rectangle {:color color})
                           (doseq [marker @markers]
                             (set!
                               (-> marker .-_icon .-style .-backgroundColor) color)
                             (set!
                               (-> marker .-_icon .-style .-display) display))))
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
                       (-> @rectangle
                         (L/off "click")
                         (L/off "dblclick"))
                       (L/remove-layer layer-container @rectangle))

        render (fn [] [:div {:style {:display "none"}}])]
    (r/create-class
      {:display-name "slide"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))
