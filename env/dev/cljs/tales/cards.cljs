(ns tales.cards
  (:require [reagent.core :as reagent :refer [atom]]
            [tales.views :as views]
            [devcards.core :as dc])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-doc defcard-rg deftest]]))

(defcard-rg project-page-card
            [views/project-page])

(defcard-rg editor-page-card
            [views/editor-page])

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
