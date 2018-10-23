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
  [check-db-interceptor trim-v]
  (fn [db [rect]]
    (let [screen-size (get-in db [:stage :size])
          rect-center (geometry/rect-center rect)
          new-scale (-> {:width (first screen-size)
                         :height (second screen-size)}
                      (geometry/rect-scale rect))]
      (-> db
        (assoc-in [:stage :position] [(:x rect-center) (:y rect-center)])
        (assoc-in [:stage :scale] new-scale)))))
