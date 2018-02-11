(ns tales.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [tales.core-test]))

(doo-tests 'tales.core-test)
