(ns tales.views.slide
  (:require [re-frame.core :refer [dispatch subscribe]]
            [tales.util.drag :refer [dragging]]
            [tales.util.events :as events]))

(defn rect [props]
  (let [scale (subscribe [:stage/scale])
        active? (:active? props)
        rect (:rect props)
        x (:x rect)
        y (:y rect)
        width (:width rect)
        height (:height rect)
        border-width (/ 2.5 @scale)
        marker-width (/ width 3)
        marker-height (/ height 3)
        color (if active? "#ff9900" "#3388ff")

        markers {:top-left {:x 0 :y 0 :cursor "nw-resize"}
                 :top {:x 1 :y 0 :cursor "n-resize"}
                 :top-right {:x 2 :y 0 :cursor "ne-resize"}
                 :right {:x 2 :y 1 :cursor "e-resize"}
                 :bottom-right {:x 2 :y 2 :cursor "se-resize"}
                 :bottom {:x 1 :y 2 :cursor "s-resize"}
                 :bottom-left {:x 0 :y 2 :cursor "sw-resize"}
                 :left {:x 0 :y 1 :cursor "w-resize"}}

        start-move (fn [e]
                     (if active?
                       (let [on-move (:on-move props)
                             on-move-end (:on-move-end props)]
                         (dragging e on-move on-move-end)
                         (events/stop e))))

        start-resize (fn [corner e]
                       (if active?
                         (let [on-resize (:on-resize props)
                               on-resize-end (:on-resize-end props)]
                           (dragging e #(on-resize corner %) on-resize-end)
                           (events/stop e))))]
    [:g {:on-click #(dispatch [:activate-slide (:key props)])
         :on-double-click #(dispatch [:stage/fit-rect rect])
         :class (:key props)}
     [:rect {:x x
             :y y
             :width width
             :height height
             :stroke color
             :stroke-width border-width
             :fill color
             :fill-opacity "0.2"}]
     (if active?
       [:g
        [:rect {:on-mouse-down start-move
                :x (+ x marker-width)
                :y (+ y marker-height)
                :width marker-width
                :height marker-height
                :fill color
                :fill-opacity 0
                :style {:cursor "move"}}]
        (for [[position options] markers]
          ^{:key position}
          [:rect {:on-mouse-down #(start-resize position %)
                  :x (+ x (* (:x options) marker-width))
                  :y (+ y (* (:y options) marker-height))
                  :width marker-width
                  :height marker-height
                  :fill color
                  :fill-opacity 0
                  :style {:cursor (:cursor options)}}])])]))