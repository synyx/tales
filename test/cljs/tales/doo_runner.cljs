(ns tales.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tales.views-test]
            [tales.geometry-test]
            [tales.events.core-test]
            [tales.events.project-test]
            [tales.events.slide-test]
            [tales.events.view-test]))

(doo-tests
  'tales.views-test
  'tales.geometry-test
  'tales.events.core-test
  'tales.events.project-test
  'tales.events.slide-test
  'tales.events.view-test)
