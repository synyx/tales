(ns tales.views.components
  (:require [reagent.core :as r]))

(defn loader [props]
  (let [color (or (:color props) "#000")
        background-color (or (:background-color props) "#fff")]
    [:svg {:width 100
           :hegiht 100
           :viewBox "0 0 100 100"
           :enable-background "new 0 0 0 0"}
     [:circle {:fill "none"
               :stroke color
               :stroke-width 4
               :cx 50
               :cy 50
               :r 44
               :opacity 0.5}]
     [:circle {:fill color
               :stroke background-color
               :stroke-width 3
               :cx 8
               :cy 54
               :r 6}
      [:animateTransform {:attributeName "transform"
                          :dur "2s"
                          :type "rotate"
                          :from "0 50 46"
                          :to "360 50 54"
                          :repeatCount "indefinite"}]]]))

(defn hide-loading [props]
  (let [this (r/current-component)
        loading? (:loading? props)
        color (:color props)
        background-color (:background-color props)]
    (into [:div {:style {:width "100%" :height "100%"}}
           [:div {:style {:display "flex"
                          :visibility (if loading? "visible" "hidden")
                          :opacity (if loading? 1 0)
                          :transition "visibility 0.5s, opacity 0.5s linear"
                          :align-items "center"
                          :justify-content "center"
                          :position "absolute"
                          :top 0
                          :bottom 0
                          :left 0
                          :right 0
                          :background-color background-color
                          :z-index 100}}
            [loader {:background-color background-color
                     :color color}]]]
      (r/children this))))