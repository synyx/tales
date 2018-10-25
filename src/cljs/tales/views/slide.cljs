(ns tales.views.slide
  (:require [re-frame.core :refer [dispatch subscribe]]
            [tales.util.drag :refer [dragging]]
            [tales.util.events :as events]))

(def markers {:top-left {:x 0 :y 0 :cursor "nw-resize"}
              :top {:x 1 :y 0 :cursor "n-resize"}
              :top-right {:x 2 :y 0 :cursor "ne-resize"}
              :right {:x 2 :y 1 :cursor "e-resize"}
              :bottom-right {:x 2 :y 2 :cursor "se-resize"}
              :bottom {:x 1 :y 2 :cursor "s-resize"}
              :bottom-left {:x 0 :y 2 :cursor "sw-resize"}
              :left {:x 0 :y 1 :cursor "w-resize"}})

(defn rect [props]
  (let [scale (subscribe [:camera/scale])
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

        start-move (fn [ev]
                     (if active?
                       (let [on-move (:on-move props)
                             on-move-end (:on-move-end props)]
                         (dragging ev on-move on-move-end)
                         (events/stop ev))))

        start-resize (fn [corner ev]
                       (if active?
                         (let [on-resize (:on-resize props)
                               on-resize-end (:on-resize-end props)]
                           (events/stop ev)
                           (dragging ev #(on-resize corner %) on-resize-end))))]
    [:g {:on-click #(dispatch [:slide/activate (:key props)])
         :on-double-click #(dispatch [:camera/fit-rect rect])
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
