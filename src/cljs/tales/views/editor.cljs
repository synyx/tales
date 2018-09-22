(ns tales.views.editor
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]
            [tales.geometry :as geometry]
            [tales.routes :as routes]
            [tales.views.preview :as preview]
            [tales.views.slide :as slide]
            [tales.views.stage :refer [stage]]))

(defn image-upload [project]
  [:div#image-upload
   [:h2 "You haven't uploaded a poster yet."]
   [:h3 "Please do so now to start editing your tale!"]
   [:input {:type "file"
            :on-change #(let [file (-> % .-target .-files (aget 0))
                              data {:project project :file file}]
                          (dispatch [:project/update-image data]))}]])

(defn image-size []
  [:div#image-size
   [:h2 "We couldn't determine your poster dimensions."]
   [:h3 "Please help us by manually setting them directly in the image!"]])

(defn navigator []
  (let [slides (subscribe [:slides])
        active-slide (subscribe [:active-slide])
        scale (subscribe [:stage/scale])

        svg-node (r/atom nil)
        draw-rect (r/atom nil)

        on-create (fn [{drag-start :start dx :dx dy :dy}]
                    (let [drag-start (dom/screen-point->container-point
                                       drag-start @svg-node)
                          x (/ (:x drag-start) @scale)
                          y (/ (:y drag-start) @scale)
                          dx (/ dx @scale)
                          dy (/ dy @scale)]
                      (reset! draw-rect (geometry/normalize-rect
                                          {:x x :y y :width dx :height dy}))))

        on-create-end (fn []
                        (if-let [rect @draw-rect]
                          (let [new-slide {:rect rect}]
                            (reset! draw-rect nil)
                            (dispatch [:editor/add-slide new-slide]))))

        on-move (fn [slide {dx :dx dy :dy}]
                  (let [dx (/ dx @scale)
                        dy (/ dy @scale)]
                    (reset! draw-rect (geometry/move-rect
                                        (:rect slide) dx dy))))

        on-move-end (fn [slide]
                      (if-let [rect @draw-rect]
                        (let [new-slide (assoc-in slide [:rect] rect)]
                          (reset! draw-rect nil)
                          (dispatch [:editor/update-slide new-slide]))))

        on-resize (fn [slide corner {dx :dx dy :dy}]
                    (let [dx (/ dx @scale)
                          dy (/ dy @scale)]
                      (reset! draw-rect (geometry/resize-rect
                                          (:rect slide) corner dx dy))))

        on-resize-end (fn [slide]
                        (if-let [rect @draw-rect]
                          (let [new-slide (assoc-in slide [:rect] rect)]
                            (reset! draw-rect nil)
                            (dispatch [:editor/update-slide new-slide]))))

        start-create (fn [e]
                       (if (dom/ctrl-key? e)
                         (do
                           (.stopPropagation e)
                           (dom/dragging e on-create on-create-end))))]
    (fn []
      (let [active-slide @active-slide]
        [stage
         [:svg {:ref #(reset! svg-node %)
                :style {:position "absolute"
                        :width "100%"
                        :height "100%"}
                :on-mouse-down start-create}
          (for [slide @slides]
            [slide/rect {:key (:index slide)
                         :rect (:rect slide)
                         :active? (= (:index slide) active-slide)
                         :on-move #(on-move slide %)
                         :on-move-end #(on-move-end slide)
                         :on-resize #(on-resize slide %1 %2)
                         :on-resize-end #(on-resize-end slide)}])
          (if-let [draw-rect @draw-rect]
            [slide/rect {:rect draw-rect}])]]))))

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
