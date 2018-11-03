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
        viewport-matrix (subscribe [:matrix/viewport])
        mvp-matrix (subscribe [:matrix/mvp])]
    [:div.scene {:style {:transform-origin "0 0"
                         :transform (css/transform-matrix
                                      @viewport-matrix)}}
     (into [:div.world {:style {:width (:width @dimensions)
                                :height (:height @dimensions)
                                :transform-origin "0 0"
                                :transform (css/transform-matrix
                                             @mvp-matrix)}}]
       (r/children (r/current-component)))]))

(defn viewport []
  (let [this (r/current-component)
        viewport-scale (subscribe [:viewport/scale])
        viewport-size (subscribe [:viewport/size])
        mvp-matrix (subscribe [:matrix/mvp])
        viewport-matrix (subscribe [:matrix/viewport])
        last-position (r/atom nil)
        on-move (fn [{{x :x y :y} :end}]
                  (let [[dx dy] (-> (gv/vec2 x y)
                                  (m/- @last-position)
                                  (m/div @viewport-scale))]
                    (reset! last-position [x y])
                    (dispatch [:camera/move-by [dx dy]])))
        on-move-end (fn []
                      (reset! last-position nil))
        start-move (fn [ev]
                     (let [{x :x y :y} (events/client-coord ev)]
                       (reset! last-position [x y])
                       (dragging ev on-move on-move-end)
                       (events/prevent ev)
                       (events/stop ev)))
        start-zoom (fn [ev]
                     (let [dom-node (r/dom-node this)
                           {x :x y :y} (geometry/distance
                                         (dom/offset dom-node)
                                         (events/client-coord ev))
                           position (-> @viewport-matrix
                                      (m/* @mvp-matrix)
                                      (m/invert)
                                      (g/transform-vector [x y]))]
                       (if (> 0 (:y (events/wheel-delta ev)))
                         (dispatch-debounced [:camera/zoom-in position])
                         (dispatch-debounced [:camera/zoom-out position]))))]
    (into [:div.viewport {:on-mouse-down start-move
                          :on-wheel start-zoom
                          :style {:background-color "#ddd"
                                  :overflow "hidden"
                                  :width (first @viewport-size)
                                  :height (second @viewport-size)
                                  :cursor (if @last-position
                                            "grab"
                                            "pointer")}}]
      (r/children (r/current-component)))))

(defn stage []
  (let [this (r/current-component)
        viewport-ready (subscribe [:viewport/ready?])
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
                  (if @viewport-ready
                    [viewport
                     (into
                       [scene
                        [poster]
                        [debug-layer]]
                       (r/children this))])])]
    (r/create-class {:display-name "stage"
                     :component-did-mount did-mount
                     :component-will-unmount will-unmount
                     :reagent-render render})))
