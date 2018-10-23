(ns tales.subs.stage
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.matrix :as gm]
            [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :stage/ready?
  (fn [_ _]
    true))

(reg-sub :stage/size
  (fn [db _]
    (get-in db [:stage :size])))

(reg-sub :stage/scale
  (fn [db _]
    (get-in db [:stage :scale])))

(reg-sub :stage/position
  (fn [db _]
    (get-in db [:stage :position])))

(reg-sub :stage/transform-matrix
  :<- [:stage/position]
  :<- [:stage/scale]
  (fn [[position scale] _]
    (-> gm/M32
      (g/translate position)
      (g/scale scale)
      (g/invert))))