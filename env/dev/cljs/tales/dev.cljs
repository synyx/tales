(ns ^:figwheel-no-load tales.dev
  (:require [tales.core :as core]
            [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
