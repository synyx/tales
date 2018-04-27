(ns tales.cards
  (:require-macros [devcards.core :refer [defcard defcard-rg deftest]])
  (:require [reagent.core :as reagent :refer [atom]]
            [tales.views.editor :refer [editor-page]]
            [tales.views.project :refer [project-page]]
            [devcards.core :as dc]))

(defcard-rg project-page-card
  [project-page])

(defcard-rg editor-page-card
  [editor-page])

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
