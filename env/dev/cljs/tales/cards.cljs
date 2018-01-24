(ns tales.cards
  (:require [reagent.core :as reagent :refer [atom]]
            [tales.core :as core]
            [devcards.core :as dc])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-doc defcard-rg deftest]]))

(defcard-rg editor-page-card
            [core/editor-page])

(defcard-rg tell-page-card
            [core/tell-page])

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
