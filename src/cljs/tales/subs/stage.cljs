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

(reg-sub :stage/aspect-ratio
  :<- [:stage/size]
  (fn [size _]
    (reduce / size)))

(reg-sub :stage/scale
  (fn [db _]
    (get-in db [:stage :scale])))

(reg-sub :stage/position
  (fn [db _]
    (get-in db [:stage :position])))

(reg-sub :stage/camera-matrix
  :<- [:stage/position]
  :<- [:stage/scale]
  (fn [[position scale] _]
    "Matrix to position the camera in the world."
    (-> gm/M32
      (g/translate position)
      (g/scale scale))))

(reg-sub :stage/view-matrix
  :<- [:stage/camera-matrix]
  (fn [camera-matrix _]
    "Matrix to convert from world coordinates to eye coordinates."
    (g/invert camera-matrix)))

(reg-sub :stage/projection-matrix
  :<- [:stage/aspect-ratio]
  :<- [:poster/dimensions]
  (fn [[aspect {width :width height :height}] _]
    "Matrix to convert from eye coordinates to clip coordinates."
    (let [aspect' (if (< aspect (/ width height))
                    (map #(* (/ width 2) %) [-1 (- (/ aspect)) 1 (/ aspect) -1 1])
                    (map #(* (/ height 2) %) [(- aspect) -1 aspect 1 -1 1]))]
      (-> (apply gm/ortho aspect')
        (gm/matrix44->matrix33)
        (gm/matrix32)))))

(reg-sub :stage/mvp-matrix
  :<- [:stage/view-matrix]
  :<- [:stage/projection-matrix]
  (fn [[view-matrix projection-matrix] _]
    "Combined matrix to convert from model coordinates to clip coordinates."
    (->> gm/M32 (g/* view-matrix) (g/* projection-matrix))))

(reg-sub :stage/viewport-matrix
  :<- [:stage/size]
  (fn [[width height] _]
    "Matrix to convert from clip coordinates to screen coordinates."
    (gm/viewport-matrix width height)))

(reg-sub :stage/transform-matrix
  :<- [:stage/mvp-matrix]
  :<- [:stage/viewport-matrix]
  (fn [[mvp-matrix viewport-matrix] _]
    (->> mvp-matrix (g/* viewport-matrix))))
