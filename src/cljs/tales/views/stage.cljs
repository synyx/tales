(ns tales.views.stage
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]
            [tales.views.loader :refer [hide-loading]]))

(defn- scale
  ([sxy] (str "scale(" sxy ")"))
  ([sx sy] (str "scale(" sx "," sy ")")))

(defn- translate [dx dy]
  (str "translate(" dx "px," dy "px)"))

(defn stage []
  (let [this (r/current-component)
        dimensions (subscribe [:poster/dimensions])
        file-path (subscribe [:poster/file-path])
        ready? (subscribe [:stage/ready?])
        stage-position (subscribe [:stage/position])
        stage-scale (subscribe [:stage/scale])
        img-node (r/atom nil)
        moving? (r/atom false)
        on-move (fn [original-position {dx :dx dy :dy}]
                  (let [x (- (:x original-position) (/ dx @stage-scale))
                        y (- (:y original-position) (/ dy @stage-scale))]
                    (dispatch [:stage/move-to x y])))
        on-move-end (fn []
                      (reset! moving? false))
        start-move (fn [e]
                     (let [original-position @stage-position]
                       (.stopPropagation e)
                       (reset! moving? true)
                       (dom/dragging e #(on-move original-position %) on-move-end)))
        start-zoom (fn [e]
                     (let [pos (dom/mouse-position e)
                           container-pos (dom/screen-point->container-point pos @img-node)
                           x (/ (:x container-pos) @stage-scale)
                           y (/ (:y container-pos) @stage-scale)
                           position {:x x :y y}]
                       (if (> 0 (.-deltaY e))
                         (dispatch ^:flush-dom [:stage/zoom-in position])
                         (dispatch ^:flush-dom [:stage/zoom-out position]))))
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
                                    :transform-origin "0 0 0"
                                    :transform (str
                                                 (scale @stage-scale)
                                                 " "
                                                 (translate
                                                   (- (:x @stage-position))
                                                   (- (:y @stage-position))))}}
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
