(ns tales.views.stage
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.vector :as gv]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.geometry :as geometry]
            [tales.util.async :refer [debounce]]
            [tales.util.css :as css]
            [tales.util.dom :as dom]
            [tales.util.drag :refer [dragging]]
            [tales.util.events :as events]
            [tales.views.loader :refer [hide-loading]]))

(def ^:private dispatch-debounced (debounce dispatch 40))

(defn debug-layer []
  (let [dimensions (subscribe [:poster/dimensions])
        height (:height @dimensions)
        width (:width @dimensions)]
    [:svg {:style {:position "absolute"
                   :width "100%"
                   :height "100%"}}
     (for [x (range 0 width 100)]
       ^{:key x}
       [:line {:x1 x :y1 0 :x2 x :y2 height :stroke "black"}])
     (for [y (range 0 height 100)]
       ^{:key y}
       [:line {:x1 0 :y1 y :x2 width :y2 y :stroke "black"}])]))

(defn poster []
  (let [file-path (subscribe [:poster/file-path])]
    [:img {:style {:position "absolute"
                   :width "100%"
                   :height "100%"}
           :src @file-path}]))

(defn scene []
  (let [dimensions (subscribe [:poster/dimensions])
        transform-matrix (subscribe [:stage/transform-matrix])]
    (into [:div.scene {:style {:width (:width @dimensions)
                               :height (:height @dimensions)
                               :position "relative"
                               :transform-origin "0 0"
                               :transform (css/transform-matrix
                                            @transform-matrix)}}]
      (r/children (r/current-component)))))

(defn stage []
  (let [this (r/current-component)
        ready? (subscribe [:stage/ready?])
        stage-position (subscribe [:stage/position])
        transform-matrix (subscribe [:stage/transform-matrix])
        moving? (r/atom false)
        on-move (fn [original-position {dx :dx dy :dy}]
                  (let [s (g/mag (gv/vec2 (nth @transform-matrix 0) (nth @transform-matrix 1)))
                        dxy (g/scale (gv/vec2 dx dy) (/ s))
                        position (g/- (gv/vec2 original-position) dxy)]
                    (dispatch [:stage/move-to position])))
        on-move-end (fn []
                      (reset! moving? false))
        start-move (fn [ev]
                     (let [original-position @stage-position
                           on-move #(on-move original-position %)]
                       (reset! moving? true)
                       (dragging ev on-move on-move-end)
                       (events/prevent ev)
                       (events/stop ev)))
        start-zoom (fn [ev]
                     (let [dom-node (r/dom-node this)
                           mouse-position (geometry/distance
                                            (dom/offset dom-node)
                                            (events/client-coord ev))
                           position (-> @transform-matrix
                                      (g/invert)
                                      (g/transform-vector
                                        [(:x mouse-position)
                                         (:y mouse-position)]))]
                       (if (> 0 (:y (events/wheel-delta ev)))
                         (dispatch-debounced [:stage/zoom-in position])
                         (dispatch-debounced [:stage/zoom-out position]))))
        on-resize (fn []
                    (let [size (dom/size (r/dom-node this))]
                      (dispatch-debounced [:stage/set-size size])))
        did-mount (fn []
                    (events/on "resize" on-resize)
                    (on-resize))
        will-unmount (fn [] (events/off "resize" on-resize))
        render (fn []
                 [:div.stage {:on-mouse-down start-move
                              :on-wheel start-zoom
                              :style {:background-color "#ddd"
                                      :overflow "hidden"
                                      :width "100%"
                                      :height "100%"
                                      :cursor (if @moving? "grab" "pointer")}}
                  [hide-loading {:loading? (not @ready?)
                                 :background-color "#ddd"
                                 :color "#fff"}
                   (into
                     [scene
                      [poster]
                      [debug-layer]]
                     (r/children this))]])]
    (r/create-class {:display-name "stage"
                     :component-did-mount did-mount
                     :component-will-unmount will-unmount
                     :reagent-render render})))
