(ns tales.subs.view
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.matrix :as gm]
            [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :stage/ready?
  (fn [_ _]
    true))

(reg-sub :viewport/size
  (fn [db _]
    (get-in db [:viewport :size])))

(reg-sub :viewport/aspect-ratio
  :<- [:viewport/size]
  (fn [size _]
    (reduce / size)))

(reg-sub :camera/position
  (fn [db _]
    (get-in db [:camera :position])))

(reg-sub :camera/scale
  (fn [db _]
    (get-in db [:camera :scale])))

(reg-sub :matrix/camera
  :<- [:camera/position]
  :<- [:camera/scale]
  (fn [[position scale] _]
    "Matrix to position the camera in the world."
    (-> gm/M32
      (g/translate position)
      (g/scale scale))))

(reg-sub :matrix/view
  :<- [:matrix/camera]
  (fn [camera-matrix _]
    "Matrix to convert from world coordinates to eye coordinates."
    (g/invert camera-matrix)))

(reg-sub :matrix/projection
  :<- [:viewport/aspect-ratio]
  :<- [:poster/dimensions]
  (fn [[aspect {width :width height :height}] _]
    "Matrix to convert from eye coordinates to clip coordinates."
    (let [aspect' (if (< aspect (/ width height))
                    (map #(* (/ width 2) %) [-1 (- (/ aspect)) 1 (/ aspect) -1 1])
                    (map #(* (/ height 2) %) [(- aspect) -1 aspect 1 -1 1]))]
      (-> (apply gm/ortho aspect')
        (gm/matrix44->matrix33)
        (gm/matrix32)))))

(reg-sub :matrix/mvp
  :<- [:matrix/view]
  :<- [:matrix/projection]
  (fn [[view-matrix projection-matrix] _]
    "Combined matrix to convert from model coordinates to clip coordinates."
    (->> gm/M32 (g/* view-matrix) (g/* projection-matrix))))

(reg-sub :matrix/viewport
  :<- [:matrix/mvp]
  :<- [:viewport/size]
  (fn [[mvp-matrix [width height]] _]
    "Combined matrix to convert from model coordinates to screen coordinates."
    (->> mvp-matrix (g/* (gm/viewport-matrix width height)))))
