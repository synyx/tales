(ns tales.events.stage
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.vector :as gv]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project check-db-interceptor]]
            [tales.geometry :as geometry]))

(reg-event-db :stage/set-size
  [check-db-interceptor trim-v]
  (fn [db [size]]
    (assoc-in db [:stage :size] size)))

(reg-event-db :stage/move-to
  [check-db-interceptor trim-v]
  (fn [db [[x y]]]
    (assoc-in db [:stage :position] [x y])))

(reg-event-db :stage/set-scale
  [check-db-interceptor trim-v]
  (fn [db [scale position]]
    (let [position (or position [0 0])
          old-position (get-in db [:stage :position])
          old-scale (get-in db [:stage :scale])
          s (/ scale old-scale)
          moved-by-scale (-> (gv/vec2 position)
                           (g/- (g/scale (gv/vec2 position) s)))
          [x y] (-> (gv/vec2 old-position)
                  (g/scale s)
                  (g/+ moved-by-scale))]
      (-> db
        (assoc-in [:stage :scale] scale)
        (assoc-in [:stage :position] [x y])))))

(reg-event-fx :stage/zoom-in
  [trim-v]
  (fn [{db :db} [position]]
    (let [scale (get-in db [:stage :scale])]
      {:dispatch [:stage/set-scale (/ scale 2) position]})))

(reg-event-fx :stage/zoom-out
  [trim-v]
  (fn [{db :db} [position]]
    (let [scale (get-in db [:stage :scale])]
      {:dispatch [:stage/set-scale (* scale 2) position]})))

(reg-event-db :stage/fit-rect
  [check-db-interceptor trim-v active-project]
  (fn [db [rect active-project]]
    (let [screen-size (get-in db [:stage :size])
          dimensions (:dimensions active-project)
          rect-center (geometry/rect-center rect)
          s (Math/min
              (/ (first screen-size) (:width dimensions))
              (/ (second screen-size) (:height dimensions)))
          new-scale (apply Math/max (-> [(:width rect) (:height rect)]
                                      (gv/vec2)
                                      (g/scale s)
                                      (g/div screen-size)))]
      (-> db
        (assoc-in [:stage :position] [(:x rect-center) (:y rect-center)])
        (assoc-in [:stage :scale] new-scale)))))
