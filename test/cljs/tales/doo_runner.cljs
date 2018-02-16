(ns tales.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tales.views-test]))

(doo-tests 'tales.views-test)
