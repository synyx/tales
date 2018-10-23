(ns tales.subs.stage
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.matrix :as gm]
            [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]
            [tales.geometry :as geometry]
            [tales.util.transform :as transform]))

(reg-sub :stage/ready?
  (fn [_ _]
    true))

(reg-sub :stage/size
  (fn [db _]
    (get-in db [:stage :size])))

(reg-sub :stage/zoom
  (fn [db _]
    (get-in db [:stage :zoom])))

(reg-sub :stage/scale
  (fn [db _]
    (geometry/zoom->scale (get-in db [:stage :zoom]))))

(reg-sub :stage/position
  (fn [db _]
    (get-in db [:stage :position])))

(reg-sub :stage/transform-origin
  (fn [db _]
    (get-in db [:stage :origin])))

(reg-sub :stage/transform-matrix
  (fn [db _]
    (let [position (get-in db [:stage :position])
          origin (get-in db [:stage :origin])
          scale (geometry/zoom->scale (get-in db [:stage :zoom]))
          moved-by-origin (transform/moved-by-origin origin scale)
          moved-position (geometry/add-points position moved-by-origin)]
      (-> gm/M32
        (g/translate
          (* (- (:x moved-position)) scale)
          (* (- (:y moved-position)) scale))
        (g/scale scale)))))