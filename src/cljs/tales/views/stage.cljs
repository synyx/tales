(ns tales.views.stage
  (:require [reagent.core :as r]
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
        stage-scale (subscribe [:stage/scale])
        stage-position (subscribe [:stage/position])
        transform-origin (subscribe [:stage/transform-origin])
        height (:height @dimensions)
        width (:width @dimensions)
        radius (/ 10 @stage-scale)]
    [:svg {:style {:position "absolute"
                   :width "100%"
                   :height "100%"}}
     [:circle {:cx (:x @stage-position)
               :cy (:y @stage-position)
               :r radius
               :fill "yellow"}]
     [:circle {:cx (:x @transform-origin)
               :cy (:y @transform-origin)
               :r radius
               :fill "red"}]
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
        transform-matrix (subscribe [:stage/transform-matrix])
        transform-origin (subscribe [:stage/transform-origin])]
    (into [:div.scene {:style {:width (:width @dimensions)
                               :height (:height @dimensions)
                               :position "relative"
                               :transform-origin (css/transform-origin
                                                   (:x @transform-origin)
                                                   (:y @transform-origin))
                               :transform (css/transform-matrix
                                            @transform-matrix)}}]
      (r/children (r/current-component)))))

(defn stage []
  (let [this (r/current-component)
        ready? (subscribe [:stage/ready?])
        stage-position (subscribe [:stage/position])
        stage-scale (subscribe [:stage/scale])
        moving? (r/atom false)
        on-move (fn [original-position {dx :dx dy :dy}]
                  (let [x (- (:x original-position) (/ dx @stage-scale))
                        y (- (:y original-position) (/ dy @stage-scale))]
                    (dispatch [:stage/move-to x y])))
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
                           position (-> mouse-position
                                      (geometry/scale @stage-scale)
                                      (geometry/add-points @stage-position))]
                       (if (> 0 (:y (events/wheel-delta ev)))
                         (dispatch-debounced [:stage/zoom-in-around position])
                         (dispatch-debounced [:stage/zoom-out-around position]))))
        on-resize (fn []
                    (let [size (dom/size (r/dom-node this))]
                      (dispatch-debounced [:stage/set-size size])))
        did-mount (fn []
                    (events/on "resize" on-resize)
                    (on-resize))
        will-unmount (fn [] (events/off "resize" on-resize))
        render (fn []
                 [:div.stage {:on-wheel start-zoom
                              :on-mouse-down start-move
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
