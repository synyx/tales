(ns tales.events.view
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.core.vector :as gv]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project check-db-interceptor]]
            [tales.geometry :as geometry]))

(reg-event-db :viewport/set-size
  [check-db-interceptor trim-v]
  (fn [db [size]]
    (assoc-in db [:viewport :size] size)))

(reg-event-db :camera/set-aspect-ratio
  [check-db-interceptor trim-v]
  (fn [db [size]]
    (assoc-in db [:camera :aspect-ratio] size)))

(reg-event-db :camera/move-to
  [check-db-interceptor trim-v]
  (fn [db [[x y]]]
    (assoc-in db [:camera :position] [x y])))

(reg-event-db :camera/set-scale
  [check-db-interceptor trim-v]
  (fn [db [scale position]]
    (let [position (or position [0 0])
          old-position (get-in db [:camera :position])
          old-scale (get-in db [:camera :scale])
          s (/ scale old-scale)
          moved-by-scale (-> (gv/vec2 position)
                           (g/- (g/scale (gv/vec2 position) s)))
          [x y] (-> (gv/vec2 old-position)
                  (g/scale s)
                  (g/+ moved-by-scale))]
      (-> db
        (assoc-in [:camera :position] [x y])
        (assoc-in [:camera :scale] scale)))))

(reg-event-fx :camera/zoom-in
  [trim-v]
  (fn [{db :db} [position]]
    (let [scale (get-in db [:camera :scale])]
      {:dispatch [:camera/set-scale (/ scale 2) position]})))

(reg-event-fx :camera/zoom-out
  [trim-v]
  (fn [{db :db} [position]]
    (let [scale (get-in db [:camera :scale])]
      {:dispatch [:camera/set-scale (* scale 2) position]})))

(reg-event-db :camera/fit-rect
  [check-db-interceptor trim-v active-project]
  (fn [db [rect active-project]]
    (let [[sw sh] (get-in db [:viewport :size])
          aspect-ratio (or
                         (get-in db [:camera :aspect-ratio])
                         (get-in db [:viewport :size]))
          aspect-factor (reduce / aspect-ratio)
          {pw :width ph :height} (:dimensions active-project)
          rect-center (geometry/rect-center rect)
          a (/ aspect-factor (/ pw ph))
          [sx sy] (if (>= aspect-factor 1)
                    [(/ (/ sw pw) a) (/ sh ph)]
                    [(/ sw pw) (* (/ sh ph) a)])
          new-scale (apply Math/max (-> [(:width rect) (:height rect)]
                                      (gv/vec2)
                                      (g/scale sx sy)
                                      (g/div [sw sh])))]
      (-> db
        (assoc-in [:camera :position] [(:x rect-center) (:y rect-center)])
        (assoc-in [:camera :scale] new-scale)))))
