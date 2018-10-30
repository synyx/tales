(ns tales.subs.view
  (:require [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.matrix :as gm]
            [thi.ng.geom.vector :as gv]
            [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :stage/ready?
  (fn [_ _]
    true))

(reg-sub :viewport/original-size
  (fn [db _]
    (get-in db [:viewport :size])))

(reg-sub :viewport/size
  :<- [:viewport/original-size]
  :<- [:camera/aspect-factor]
  (fn [[[w h] camera-aspect-factor] _]
    (if (>= (/ (/ w h) camera-aspect-factor) 1)
      [(* h camera-aspect-factor) h]
      [w (/ w camera-aspect-factor)])))

(reg-sub :viewport/scale
  :<- [:matrix/viewport]
  (fn [m _]
    (-> (gv/vec2 (nth m 0) (nth m 1))
      (m/mag))))

(reg-sub :camera/aspect-ratio
  (fn [db _]
    (or
      (get-in db [:camera :aspect-ratio])
      (get-in db [:viewport :size]))))

(reg-sub :camera/aspect-factor
  :<- [:camera/aspect-ratio]
  (fn [[x y] _]
    (if (not= y 0)
      (/ x y)
      1)))

(reg-sub :camera/position
  (fn [db _]
    "The position of the camera in world space."
    (get-in db [:camera :position])))

(reg-sub :camera/scale
  (fn [db _]
    "The scale of the camera in world space. A smaller value shrinks the view
    of the camera and therefore magnifies the resulting view."
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
    (m/invert camera-matrix)))

(reg-sub :matrix/projection
  :<- [:camera/aspect-factor]
  (fn [a _]
    "Matrix to convert from eye coordinates to clip coordinates."
    (let [[l t r b n f] (if (>= a 1)
                          [0 0 1 (/ a) 1 -1]
                          [0 0 (* a) 1 1 -1])]
      (-> (gm/ortho l t r b n f)
        (gm/matrix44->matrix33)
        (gm/matrix32)))))

(reg-sub :matrix/mvp
  :<- [:matrix/view]
  :<- [:matrix/projection]
  (fn [[view-matrix projection-matrix] _]
    "Combined matrix to convert from model coordinates to clip coordinates."
    (->> gm/M32 (m/* view-matrix) (m/* projection-matrix))))

(reg-sub :matrix/viewport
  :<- [:matrix/mvp]
  :<- [:viewport/size]
  (fn [[mvp-matrix [width height]] _]
    "Combined matrix to convert from model coordinates to screen coordinates."
    (->> mvp-matrix (m/* (gm/viewport-matrix width height)))))
