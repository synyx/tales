(ns tales.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tales.views-test]
            [tales.geometry-test]
            [tales.events.core-test]
            [tales.events.slide-test]
            [tales.events.stage-test]))

(doo-tests
  'tales.views-test
  'tales.geometry-test
  'tales.events.core-test
  'tales.events.slide-test
  'tales.events.stage-test)
