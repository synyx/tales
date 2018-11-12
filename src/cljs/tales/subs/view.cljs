(ns tales.subs.view
  (:require-macros [reagent.ratom :as ratom])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.matrix :as gm]
            [thi.ng.geom.vector :as gv]))

(reg-sub-raw :viewport/ready?
  (fn [db _]
    (let [had-size? (r/atom false)
          setup-camera #(dispatch [:camera/setup])
          destroy-camera #(dispatch [:camera/destroy])
          has? (fn [key m] (not (nil? (key m))))]
      (ratom/reaction
        (let [viewport (get @db :viewport)
              camera (get @db :camera)
              has-size? (has? :size viewport)
              ready? (and has-size?
                       (has? :position camera)
                       (has? :scale camera))]
          (if @had-size?
            (when-not has-size? (destroy-camera))
            (when has-size? (setup-camera)))
          (reset! had-size? has-size?)
          ready?)))))

(reg-sub :viewport/size
  (fn [db _]
    (get-in db [:viewport :size] [1 1])))

(reg-sub :viewport/view-rect
  :<- [:viewport/size]
  (fn [size _]
    {:pos [0 0]
     :size size}))

(reg-sub :viewport/aspect-ratio
  :<- [:viewport/size]
  (fn [[width height] _]
    (/ width height)))

(reg-sub :viewport/scale
  :<- [:matrix/mvp]
  :<- [:matrix/viewport]
  (fn [[m1 m2] _]
    (-> (gv/vec3 (nth m2 0) (nth m2 1) (nth m2 2))
      (m/* (gv/vec3 (nth m1 0) (nth m1 1) (nth m1 2)))
      (m/mag))))

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
    (-> gm/M44
      (g/translate position)
      (g/scale [scale scale 1]))))

(reg-sub :matrix/view
  :<- [:matrix/camera]
  (fn [camera-matrix _]
    "Matrix to convert from world coordinates to eye coordinates."
    (m/invert camera-matrix)))

(reg-sub :matrix/projection
  :<- [:viewport/aspect-ratio]
  (fn [a _]
    "Matrix to convert from eye coordinates to clip coordinates."
    (gm/ortho (- a) -1 a 1 -1 1)))

(reg-sub :matrix/mvp
  :<- [:matrix/view]
  :<- [:matrix/projection]
  (fn [[view-matrix projection-matrix] _]
    "Combined matrix to convert from model coordinates to clip coordinates."
    (m/* projection-matrix view-matrix)))

(reg-sub :matrix/viewport
  :<- [:viewport/size]
  (fn [[width height] _]
    "Combined matrix to convert from model coordinates to screen coordinates."
    (gm/viewport-matrix width height)))
