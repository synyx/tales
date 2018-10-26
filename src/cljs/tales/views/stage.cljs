(ns tales.views.stage
  (:require [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as gv]
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
        viewport-matrix (subscribe [:matrix/viewport])]
    (into [:div.scene {:style {:width (:width @dimensions)
                               :height (:height @dimensions)
                               :position "relative"
                               :transform-origin "0 0"
                               :transform (css/transform-matrix
                                            @viewport-matrix)}}]
      (r/children (r/current-component)))))

(defn viewport []
  (let [this (r/current-component)
        stage-position (subscribe [:camera/position])
        viewport-matrix (subscribe [:matrix/viewport])
        viewport-scale (subscribe [:viewport/scale])
        viewport-size (subscribe [:viewport/size])
        moving? (r/atom false)
        on-move (fn [original-position {dx :dx dy :dy}]
                  (let [dxy (-> (gv/vec2 dx dy)
                              (m/div @viewport-scale))
                        position (m/- (gv/vec2 original-position) dxy)]
                    (dispatch [:camera/move-to position])))
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
                           {x :x y :y} (geometry/distance
                                         (dom/offset dom-node)
                                         (events/client-coord ev))
                           position (-> @viewport-matrix
                                      (m/invert)
                                      (g/transform-vector [x y]))]
                       (if (> 0 (:y (events/wheel-delta ev)))
                         (dispatch-debounced [:camera/zoom-in position])
                         (dispatch-debounced [:camera/zoom-out position]))))]
    (into [:div.scene {:on-mouse-down start-move
                       :on-wheel start-zoom
                       :style {:background-color "#ddd"
                               :position "relative"
                               :overflow "hidden"
                               :width (first @viewport-size)
                               :height (second @viewport-size)
                               :cursor (if @moving? "grab" "pointer")}}]
      (r/children (r/current-component)))))

(defn stage []
  (let [this (r/current-component)
        on-resize (fn []
                    (let [size (dom/size (r/dom-node this))]
                      (dispatch-debounced [:viewport/set-size size])))
        did-mount (fn []
                    (events/on "resize" on-resize)
                    (on-resize))
        will-unmount (fn [] (events/off "resize" on-resize))
        render (fn []
                 [:div.stage {:style {:background-color "#233"
                                      :display "flex"
                                      :align-items "center"
                                      :justify-content "center"
                                      :width "100%"
                                      :height "100%"}}
                  [viewport
                   (into
                     [scene
                      [poster]
                      [debug-layer]]
                     (r/children this))]])]
    (r/create-class {:display-name "stage"
                     :component-did-mount did-mount
                     :component-will-unmount will-unmount
                     :reagent-render render})))
