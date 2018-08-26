(ns tales.cards
  (:require-macros [devcards.core :refer [defcard defcard-rg deftest]])
  (:require [reagent.core :as reagent :refer [atom]]
            [tales.views.editor :as editor]
            [tales.views.project :as project]
            [devcards.core :as dc]))

(defcard-rg editor-page-card
  [editor/page])

(defcard-rg project-page-card
  [project/page])

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
