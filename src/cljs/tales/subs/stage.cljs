(ns tales.subs.stage
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]
            [tales.geometry :as geometry]
            [tales.util.transform :as transform]))

(reg-sub :stage/ready?
  (fn [db _]
    (let [dom-node (get-in db [:stage :dom-node])]
      (not (nil? dom-node)))))

(reg-sub :stage/dom-node
  (fn [db _]
    (get-in db [:stage :dom-node])))

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
      [scale
       0
       0
       scale
       (* (- (:x moved-position)) scale)
       (* (- (:y moved-position)) scale)])))