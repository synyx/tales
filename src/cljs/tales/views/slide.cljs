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

(defn marker [_ layer-container]
  (let [marker (r/atom nil)
        icon (r/atom nil)

        will-mount (fn [this]
                     (let [latlng (:latlng (r/props this))]
                       (reset! icon
                         (L/create-div-icon {:className "slide-resize-marker"}))
                       (reset! marker
                         (L/create-marker latlng {:icon @icon
                                                  :draggable true}))))

        did-mount (fn [this]
                    (let [corner (:corner (r/props this))
                          on-move (:on-move (r/props this))]
                      (L/add-layer layer-container @marker)
                      (L/on @marker "mousedown" #(on-move corner %))))

        did-update (fn [this [_ prev-props]]
                     (if-not (= prev-props (r/props this))
                       (L/set-latlng @marker (:latlng (r/props this)))))

        will-unmount (fn []
                       (L/off @marker "mousedown")
                       (L/remove-layer layer-container @marker))

        render (fn [])]
    (r/create-class
      {:display-name "slide-marker"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn rect [props layer-container slide]
  (let [rectangle (r/atom nil)
        slide (r/atom slide)

        start-move (fn [e]
                     (-> layer-container .-_map .-dragging .disable)
                     (dispatch [:activate-slide (:index @slide)])
                     (dispatch [:start-draw :move @slide (.-latlng e)]))

        start-resize (fn [corner e]
                       (-> layer-container .-_map .-dragging .disable)
                       (dispatch [:activate-slide (:index @slide)])
                       (dispatch [:start-draw :resize @slide (.-latlng e) corner]))

        on-dblclick #(do (dispatch [:move-to-slide (:index @slide)])
                         (.stopPropagation js/L.DomEvent %))

        set-props (fn [props]
                    (let [color (if (:active? props) "#ff9900" "#3388ff")]
                      (L/set-style @rectangle {:color color})))

        will-mount (fn []
                     (let [bounds (L/slide-rect->latlng-bounds (:rect @slide))
                           active? (:active? props)
                           color (if active? "#ff9900" "#3388ff")]
                       (reset! rectangle (L/create-rectangle bounds))
                       (-> @rectangle
                         (L/set-style {:color color})
                         (L/on "mousedown" start-move)
                         (L/on "dblclick" on-dblclick))))

        did-mount (fn []
                    (L/add-layer layer-container @rectangle))

        did-update (fn [this [_ prev-props _ prev-slide]]
                     (let [[_ _ _ updated-slide] (r/argv this)]
                       (if-not (= prev-props (r/props this))
                         (set-props (r/props this)))
                       (if-not (= prev-slide updated-slide)
                         (do
                           (reset! slide updated-slide)
                           (L/set-bounds @rectangle (L/slide-rect->latlng-bounds (:rect @slide)))))))

        will-unmount (fn []
                       (-> @rectangle
                         (L/off "dblclick")
                         (L/off "mousedown"))
                       (L/remove-layer layer-container @rectangle))

        render (fn [props]
                 (let [bounds (L/slide-rect->latlng-bounds (:rect @slide))
                       corners (corners bounds)]
                   [:div {:style {:display "none"}}
                    (if (:active? props)
                      [:div
                       [marker {:corner :north-west
                                :latlng (:north-west corners)
                                :on-move start-resize} layer-container]
                       [marker {:corner :north-east
                                :latlng (:north-east corners)
                                :on-move start-resize} layer-container]
                       [marker {:corner :south-east
                                :latlng (:south-east corners)
                                :on-move start-resize} layer-container]
                       [marker {:corner :south-west
                                :latlng (:south-west corners)
                                :on-move start-resize} layer-container]])]))]
    (r/create-class
      {:display-name "slide"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))
