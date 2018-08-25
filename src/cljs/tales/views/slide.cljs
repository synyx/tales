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

(defn rect [props layer-container slide]
  (let [rectangle (r/atom nil)
        slide (r/atom slide)
        markers (r/atom {})

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

        create-markers (fn [bounds]
                         (let [icon (L/create-div-icon {:className "slide-resize-marker"})
                               options {:icon icon :draggable true}]
                           (doseq [[corner latlng] (corners bounds)]
                             (reset! markers
                               (assoc @markers corner (L/create-marker latlng options)))
                             (L/on (get @markers corner) "mousedown" #(start-resize corner %)))))

        set-props (fn [props]
                    (let [active? (:active? props)
                          color (if active? "#ff9900" "#3388ff")
                          display (if active? "block" "none")]
                      (L/set-style @rectangle {:color color})
                      (doseq [[_ marker] @markers]
                        (set!
                          (-> marker .-_icon .-style .-backgroundColor) color)
                        (set!
                          (-> marker .-_icon .-style .-display) display))))

        set-bounds (fn [bounds]
                     (L/set-bounds @rectangle bounds)
                     (doseq [[corner latlng] (corners bounds)]
                       (L/set-latlng (get @markers corner) latlng)))

        will-mount (fn []
                     (let [bounds (L/slide-rect->latlng-bounds (:rect @slide))
                           active? (:active? props)
                           color (if active? "#ff9900" "#3388ff")]
                       (reset! rectangle (L/create-rectangle bounds))
                       (-> @rectangle
                         (L/set-style {:color color})
                         (L/on "mousedown" start-move)
                         (L/on "dblclick" on-dblclick))
                       (create-markers (L/slide-rect->latlng-bounds (:rect @slide)))))

        did-mount (fn []
                    (L/add-layer layer-container @rectangle)
                    (doseq [[_ marker] @markers]
                      (L/add-layer layer-container marker)))

        did-update (fn [this [_ prev-props _ prev-slide]]
                     (let [[_ _ _ updated-slide] (r/argv this)]
                       (if-not (= prev-props (r/props this))
                         (set-props (r/props this)))
                       (if-not (= prev-slide updated-slide)
                         (do
                           (reset! slide updated-slide)
                           (set-bounds (L/slide-rect->latlng-bounds (:rect @slide)))))))

        will-unmount (fn []
                       (doseq [[_ marker] @markers]
                         (L/off marker "mousedown")
                         (L/remove-layer layer-container marker))
                       (-> @rectangle
                         (L/off "dblclick")
                         (L/off "mousedown"))
                       (L/remove-layer layer-container @rectangle))

        render (fn [] [:div {:style {:display "none"}}])]
    (r/create-class
      {:display-name "slide"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-did-update did-update
       :component-will-unmount will-unmount
       :reagent-render render})))
