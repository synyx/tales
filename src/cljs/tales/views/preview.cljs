(ns tales.views.preview
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn- slide-scale [slide target-width target-height]
  "Calculates the scale factor to resize the slide to target-width/-height"
  (let [slide-width (get-in slide [:rect :width])
        slide-height (get-in slide [:rect :height])]
    (if (> (/ target-width target-height) (/ slide-width slide-height))
      (/ target-height slide-height)
      (/ target-width slide-width))))

(defn slide []
  (let [this (r/current-component)
        did-update (fn []
                     (let [props (r/props this)
                           active? (:active? props)]
                       (if active?
                         (.scrollIntoView (r/dom-node this)))))
        render (fn [props project slide]
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
                    {:style {:position "relative"
                             :width preview-width :min-width preview-width
                             :height preview-height :min-height preview-height
                             :background-color "#333"
                             :border-width 3
                             :border-style "solid"
                             :border-color (if active? "#ff0000" "#333")}
                     :on-click #(dispatch [:slide/activate (:index slide)])
                     :on-double-click #(dispatch [:camera/fly-to-rect (:rect slide)])}
                    [:div
                     {:style {:width scaled-slide-width
                              :height scaled-slide-height
                              :background-color "#fff"
                              :background-repeat "no-repeat"
                              :background-image (str "url(" (:file-path project) ")")
                              :background-size (str
                                                 scaled-img-width "px "
                                                 scaled-img-height "px")
                              :background-position-x (- dx)
                              :background-position-y (- dy)}}]
                    [:div {:style {:position "absolute"
                                   :background-color "rgba(0,0,0,0.6)"
                                   :color "#fff"
                                   :line-height "1em"
                                   :font-size "0.8em"
                                   :font-style "monospace"
                                   :left "0"
                                   :bottom "0"
                                   :padding "4px 4px 3px 3px"}}
                     (inc (:index slide))]]))]
    (r/create-class {:display-name "slide"
                     :component-did-update did-update
                     :reagent-render render})))

(defn slides [project]
  (let [slides (subscribe [:slides])
        active-slide (subscribe [:slide/active])
        preview-width 100
        preview-height 75]
    (let [active-slide @active-slide]
      [:div#slides-preview.slide-preview-list
       (for [item @slides]
         ^{:key (:index item)}
         [slide {:width preview-width
                 :height preview-height
                 :active? (= (:index item) active-slide)}
          project item])])))
