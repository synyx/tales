(ns tales.views.stage
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]
            [tales.geometry :as geometry]
            [tales.util.async :refer [debounce]]
            [tales.util.css :as css]
            [tales.util.drag :refer [dragging]]
            [tales.util.events :as events]
            [tales.views.loader :refer [hide-loading]]))

(defn- zoom [direction position]
  (dispatch [(case direction
               :in :stage/zoom-in-around
               :out :stage/zoom-out-around) position]))
(def ^:private zoom-debounced (debounce zoom 40))

(defn stage []
  (let [this (r/current-component)
        dimensions (subscribe [:poster/dimensions])
        file-path (subscribe [:poster/file-path])
        ready? (subscribe [:stage/ready?])
        stage-position (subscribe [:stage/position])
        stage-scale (subscribe [:stage/scale])
        transform-origin (subscribe [:stage/transform-origin])
        transform-matrix (subscribe [:stage/transform-matrix])
        img-node (r/atom nil)
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
                       (events/stop ev)))
        start-zoom (fn [ev]
                     (let [position (-> (events/client-coord ev)
                                      (dom/screen-point->node-point @img-node)
                                      (geometry/scale @stage-scale))]
                       (if (> 0 (:y (events/wheel-delta ev)))
                         (zoom-debounced :in position)
                         (zoom-debounced :out position))))
        did-mount (fn [] (dispatch [:stage/mounted (r/dom-node this)]))
        will-unmount (fn [] (dispatch [:stage/unmounted]))
        render (fn []
                 [:div {:on-wheel start-zoom
                        :on-mouse-down start-move
                        :style {:background-color "#ddd"
                                :overflow "hidden"
                                :width "100%"
                                :height "100%"
                                :position "relative"
                                :cursor (if @moving? "grab" "pointer")}}
                  [hide-loading {:loading? (not @ready?)
                                 :background-color "#ddd"
                                 :color "#fff"}
                   (into
                     [:div {:style {:width (:width @dimensions)
                                    :height (:height @dimensions)
                                    :position "absolute"
                                    :left "50%"
                                    :top "50%"
                                    :transform-origin (css/transform-origin
                                                        (:x @transform-origin)
                                                        (:y @transform-origin))
                                    :transform (apply css/transform-matrix
                                                 @transform-matrix)}}
                      [:img {:ref #(reset! img-node %)
                             :style {:position "absolute"
                                     :width "100%"
                                     :height "100%"}
                             :src @file-path}]]
                     (r/children this))]])]
    (r/create-class {:display-name "stage"
                     :component-did-mount did-mount
                     :component-will-unmount will-unmount
                     :reagent-render render})))
