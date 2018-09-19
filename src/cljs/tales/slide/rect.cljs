(ns tales.slide.rect
  (:require [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]))

(defn rect [props]
  (let [active? (:active? props)
        rect (:rect props)
        x (:x rect)
        y (:y rect)
        width (:width rect)
        height (:height rect)

        color (if active? "#ff9900" "#3388ff")

        activate-slide (fn [e]
                         (.stopPropagation e)
                         (dom/dragging e (:on-move props) (:on-move-end props)))

        start-resize (fn [corner e]
                       (let [on-resize (:on-resize props)
                             on-resize-end (:on-resize-end props)]
                         (.stopPropagation e)
                         (dom/dragging e #(on-resize corner %1 %2 %3) on-resize-end)))]
    [:g {:on-click #(dispatch [:activate-slide (:key props)])
         :on-double-click #(dispatch [:stage/fit-rect rect])
         :on-mouse-down activate-slide
         :class (:key props)}
     [:rect {:x x
             :y y
             :width width
             :height height
             :stroke color
             :stroke-width "5"
             :fill color
             :fill-opacity "0.2"}]
     (if active?
       [:g
        [:rect {:on-mouse-down #(start-resize :top-left %)
                :x x
                :y y
                :width 20
                :height 20
                :fill color}]
        [:rect {:on-mouse-down #(start-resize :top-right %)
                :x (- (+ x width) 20)
                :y y
                :width 20
                :height 20
                :fill color}]
        [:rect {:on-mouse-down #(start-resize :bottom-right %)
                :x (- (+ x width) 20)
                :y (- (+ y height) 20)
                :width 20
                :height 20
                :fill color}]
        [:rect {:on-mouse-down #(start-resize :bottom-left %)
                :x x
                :y (- (+ y height) 20)
                :width 20
                :height 20
                :fill color}]])]))