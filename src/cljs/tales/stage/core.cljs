(ns tales.stage.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]
            [tales.views.components :refer [hide-loading]]))

(defn- scale
  ([sxy] (str "scale(" sxy ")"))
  ([sx sy] (str "scale(" sx "," sy ")")))

(defn- translate [dx dy]
  (str "translate(" dx "px," dy "px)"))

(defn movable []
  (let [this (r/current-component)
        stage-scale (subscribe [:stage/scale])
        current-pos (subscribe [:stage/position])
        original-pos (r/atom nil)
        start-pos (r/atom nil)
        moving? (r/atom false)
        on-mouse-down (fn [e]
                        (reset! original-pos @current-pos)
                        (reset! start-pos (dom/mouse-position e))
                        (reset! moving? true)
                        (.preventDefault e))
        on-mouse-move (fn [e]
                        (if @moving?
                          (let [pos (dom/mouse-position e)
                                dx (/ (- (:x pos) (:x @start-pos)) @stage-scale)
                                dy (/ (- (:y pos) (:y @start-pos)) @stage-scale)
                                x (- (:x @original-pos) dx)
                                y (- (:y @original-pos) dy)]
                            (dispatch [:stage/move-to x y]))))
        on-mouse-up (fn [e]
                      (reset! moving? false)
                      (reset! start-pos nil)
                      (reset! original-pos nil)
                      (.preventDefault e))]
    (fn []
      (into [:div.movable {:style {:width "100%"
                                   :height "100%"
                                   :cursor (if @moving? "grab" "pointer")}
                           :on-mouse-down on-mouse-down
                           :on-mouse-move on-mouse-move
                           :on-mouse-up on-mouse-up}]
        (r/children this)))))

(defn stage []
  (let [this (r/current-component)
        dimensions (subscribe [:poster/dimensions])
        file-path (subscribe [:poster/file-path])
        ready? (subscribe [:stage/ready?])
        stage-position (subscribe [:stage/position])
        stage-scale (subscribe [:stage/scale])
        img-node (r/atom nil)
        did-mount (fn [] (dispatch [:stage/mounted (r/dom-node this)]))
        will-unmount (fn [] (dispatch [:stage/unmounted]))
        on-wheel (fn [e]
                   (let [pos (dom/mouse-position e)
                         container-pos (dom/screen-point->container-point pos @img-node)
                         x (/ (:x container-pos) @stage-scale)
                         y (/ (:y container-pos) @stage-scale)
                         position {:x x :y y}]
                     (if (> 0 (.-deltaY e))
                       (dispatch [:stage/zoom-in position])
                       (dispatch [:stage/zoom-out position]))))
        render (fn []
                 [:div {:on-wheel on-wheel
                        :style {:background-color "#ddd"
                                :overflow "hidden"
                                :width "100%"
                                :height "100%"
                                :position "relative"}}
                  [hide-loading {:loading? (not @ready?)
                                 :background-color "#ddd"
                                 :color "#fff"}
                   [movable
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
                      (r/children this))]]])]
    (r/create-class {:display-name "stage"
                     :component-did-mount did-mount
                     :component-will-unmount will-unmount
                     :reagent-render render})))
