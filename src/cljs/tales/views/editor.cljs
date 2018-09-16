(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.routes :as routes]
            [tales.leaflet.core :as L]
            [tales.views.preview :as preview]
            [tales.views.slide :as slide]))

(defn image-upload [project]
  [:div#image-upload
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))
                              data {:project project :file file}]
                          (dispatch [:update-project-image data]))}]])

(defn image-size []
  [:div#image-size
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn ctrl-key? [e]
  (or
    (-> e .-originalEvent .-ctrlKey)
    (-> e .-originalEvent .-metaKey)))

(defn draw-handler [map]
  (let [drawing? (subscribe [:drawing?])
        draw-start #(if (ctrl-key? %)
                      (let [start (.-latlng %)
                            slide {:rect {:x (.-lng start)
                                          :y (.-lat start)
                                          :width 0
                                          :height 0}}]
                        (do (-> map .-dragging .disable)
                            (dispatch [:start-draw :create slide start]))))
        draw-end #(do (-> map .-dragging .enable)
                      (if @drawing? (dispatch [:end-draw])))
        draw-update #(if @drawing? (dispatch [:update-draw (.-latlng %)]))]
    (-> map
      (L/on "mousedown" draw-start)
      (L/on "mouseup" draw-end)
      (L/on "mousemove" draw-update)
      (L/on "touchstart" draw-start)
      (L/on "touchend" draw-end)
      (L/on "touchmove" draw-update))))

(defn slide-layer [layer-container]
  (let [slides (subscribe [:slides])
        current-slide (subscribe [:current-slide])
        layer (r/atom nil)
        will-mount (fn []
                     (reset! layer (L/create-feature-group)))
        did-mount (fn []
                    (L/add-layer layer-container @layer))
        will-unmount (fn []
                       (L/remove-layer layer-container @layer))
        render (fn []
                 (let [layer @layer
                       current-slide @current-slide]
                   [:div {:style {:display "none"}}
                    (for [slide @slides]
                      ^{:key (:index slide)}
                      [slide/rect
                       {:active? (= (:index slide) current-slide)}
                       layer slide])]))]
    (r/create-class
      {:display-name "slide-layer"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn draw-layer [layer-container]
  (let [draw-slide (subscribe [:draw-slide])
        layer (r/atom nil)
        will-mount (fn []
                     (reset! layer (L/create-feature-group)))
        did-mount (fn []
                    (L/add-layer layer-container @layer))
        will-unmount (fn []
                       (L/remove-layer layer-container @layer))
        render (fn []
                 [:div {:style {:display "none"}}
                  (if @draw-slide
                    [slide/rect {:color "#ff9900"} @layer @draw-slide])])]
    (r/create-class
      {:display-name "draw-layer"
       :component-will-mount will-mount
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn- mouse-pos [event]
  {:x (.-clientX event)
   :y (.-clientY event)})

(defn- scale
  ([sxy] (str "scale(" sxy ")"))
  ([sx sy] (str "scale(" sx "," sy ")")))

(defn- translate [dx dy]
  (str "translate(" dx "px," dy "px)"))

(defn zoomable []
  (let [this (r/current-component)
        on-wheel (fn [e]
                   (if (> 0 (.-deltaY e))
                     (r/next-tick #(dispatch [:stage/zoom-in]))
                     (r/next-tick #(dispatch [:stage/zoom-out]))))]
    (fn []
      (into [:div {:on-wheel on-wheel}]
        (r/children this)))))

(defn movable []
  (let [this (r/current-component)
        stage-scale (subscribe [:stage/scale])
        current-pos (subscribe [:stage/position])
        original-pos (r/atom nil)
        start-pos (r/atom nil)
        moving? (r/atom false)
        on-mouse-down (fn [e]
                        (reset! original-pos @current-pos)
                        (reset! start-pos (mouse-pos e))
                        (reset! moving? true)
                        (.preventDefault e))
        on-mouse-move (fn [e]
                        (if @moving?
                          (let [pos (mouse-pos e)
                                dx (/ (- (:x pos) (:x @start-pos)) @stage-scale)
                                dy (/ (- (:y pos) (:y @start-pos)) @stage-scale)
                                x (+ (:x @original-pos) dx)
                                y (+ (:y @original-pos) dy)]
                            (r/next-tick
                              #(dispatch [:stage/move-to x y])))))
        on-mouse-up (fn [e]
                      (reset! moving? false)
                      (reset! start-pos nil)
                      (reset! original-pos nil)
                      (.preventDefault e))]
    (fn []
      (into [:div {:style {:cursor (if @moving? "grab" "pointer")}
                   :on-mouse-down on-mouse-down
                   :on-mouse-move on-mouse-move
                   :on-mouse-up on-mouse-up}]
        (r/children this)))))

(defn stage []
  (let [this (r/current-component)
        dimensions (subscribe [:poster/dimensions])
        file-path (subscribe [:poster/file-path])
        stage-position (subscribe [:stage/position])
        stage-scale (subscribe [:stage/scale])
        dom-node (r/atom nil)

        did-mount (fn [this]
                    (reset! dom-node (r/dom-node this)))

        will-unmount (fn []
                       (reset! dom-node nil))

        render (fn []
                 [:div {:style {:background-color "#ddd"
                                :overflow "hidden"
                                :width "100%"
                                :height "100%"}}
                  (if @dom-node
                    (let [width (.-clientWidth @dom-node)
                          center-x (/ width 2)]
                      [zoomable
                       [movable
                        (into
                          [:div {:style {:width (:width @dimensions)
                                         :height (:height @dimensions)
                                         :transform-origin (str center-x "px 0 0")
                                         :transform (str
                                                      (scale @stage-scale)
                                                      " "
                                                      (translate
                                                        (:x @stage-position)
                                                        (:y @stage-position)))}}
                           [:img {:style {:position "absolute"
                                          :width "100%"
                                          :height "100%"}
                                  :src @file-path}]]
                          (r/children this))]]))])]
    (r/create-class
      {:display-name "stage"
       :component-did-mount did-mount
       :component-will-unmount will-unmount
       :reagent-render render})))

(defn navigator []
  (let [slides (subscribe [:slides])
        current-slide (subscribe [:current-slide])]
    [stage
     [:svg {:style {:position "absolute"
                    :width "100%"
                    :height "100%"}}
      (doall
        (for [slide @slides]
          (let [active? (= (:index slide) @current-slide)
                rect (:rect slide)]
            ^{:key (:index slide)}
            [:g [:rect {:x (:x rect)
                        :y (:y rect)
                        :width (:width rect)
                        :height (:height rect)
                        :stroke (if active? "#ff9900" "#3388ff")
                        :stroke-width "5"
                        :fill "none"}]])))]]))

(defn page []
  (let [project (subscribe [:active-project])]
    [:div {:id "editor"}
     [:header
      [:h1 (:name @project)]
      [:a {:href (routes/home-path)} "Close"]]
     [:main (cond
              (nil? (:file-path @project)) [image-upload @project]
              (nil? (:dimensions @project)) [image-size]
              :else [navigator])]
     [:footer [preview/slides @project]]]))
