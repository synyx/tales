(ns tales.views.preview
  (:require [re-frame.core :refer [dispatch subscribe]]
            [tales.dom :as dom]))

(defn- slide-scale [slide target-width target-height]
  "Calculates the scale factor to resize the slide to target-width/-height"
  (let [slide-width (get-in slide [:rect :width])
        slide-height (get-in slide [:rect :height])]
    (if (> (/ target-width target-height) (/ slide-width slide-height))
      (/ target-height slide-height)
      (/ target-width slide-width))))

(defn slide [props project slide]
  (let [rect (:rect slide)
        preview-width (:width props)
        preview-height (:height props)
        active? (:active? props)
        scale (slide-scale slide preview-width preview-height)
        dx (* scale (:x rect))
        dy (* scale (:y rect))
        scaled-slide-width (* scale (:width rect))
        scaled-slide-height (* scale (:height rect))
        scaled-img-width (* scale (:width (:dimensions project)))
        scaled-img-height (* scale (:height (:dimensions project)))]
    [:div.slide-preview-list-item
     {:style {:width preview-width :min-width preview-width
              :height preview-height :min-height preview-height
              :background-color "#333"
              :border-width 3
              :border-style "solid"
              :border-color (if active? "#ff0000" "#333")}
      :on-click #(dispatch [:activate-slide (:index slide)])
      :on-double-click #(dispatch [:move-to-slide (:index slide)])}
     [:div
      {:style {:width scaled-slide-width
               :height scaled-slide-height
               :background-color "#fff"
               :background-repeat "no-repeat"
               :background-image (str "url(" (:file-path project) ")")
               :background-size (str scaled-img-width "px" " " scaled-img-height "px")
               :background-position-x (- dx)
               :background-position-y (- dy)}}]]))

(defn slides [project]
  (let [slides (subscribe [:slides])
        current-slide (subscribe [:current-slide])
        preview-width 100
        preview-height 75]
    (let [current-slide @current-slide]
      [:div#slides-preview.slide-preview-list
       {:tabIndex 0
        :on-key-down #(case (.-key %)
                        "Delete" (dispatch [:delete-current-slide])
                        " " (dispatch [:next-slide])
                        "ArrowRight" (if (dom/ctrl-key? %)
                                       (dispatch [:change-order 1])
                                       (dispatch [:next-slide]))
                        "ArrowLeft" (if (dom/ctrl-key? %)
                                      (dispatch [:change-order -1])
                                      (dispatch [:prev-slide]))
                        nil)}
       (for [item @slides]
         ^{:key (:index item)}
         [slide {:width preview-width
                 :height preview-height
                 :active? (= (:index item) current-slide)}
          project item])])))
