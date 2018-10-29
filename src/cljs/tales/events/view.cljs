(ns tales.events.view
  (:require [thi.ng.math.core :as m]
            [thi.ng.geom.vector :as gv]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.animation :as anim]
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
                           (m/- (m/* (gv/vec2 position) s)))
          [x y] (-> (gv/vec2 old-position)
                  (m/* s)
                  (m/+ moved-by-scale))]
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

(defn- rect-scale [[aw ah] [bw bh] aspect-ratio]
  (let [aspect-factor (reduce / aspect-ratio)
        scale (if (>= aspect-factor 1)
                [(/ (/ aw bh) aspect-factor) (/ ah bh)]
                [(/ aw bw) (* (/ ah bw) aspect-factor)])]
    (apply Math/max scale)))

(reg-event-db :camera/fit-rect
  [check-db-interceptor trim-v active-project]
  (fn [db [rect active-project]]
    (let [aspect-ratio (or
                         (get-in db [:camera :aspect-ratio])
                         (get-in db [:viewport :size]))
          poster-rect [(get-in active-project [:dimensions :width])
                       (get-in active-project [:dimensions :height])]
          rect-center (geometry/rect-center rect)
          rect [(:width rect) (:height rect)]
          new-scale (rect-scale rect poster-rect aspect-ratio)]
      (-> db
        (assoc-in [:camera :position] [(:x rect-center) (:y rect-center)])
        (assoc-in [:camera :scale] new-scale)))))

(reg-event-fx :camera/fly-to-rect
  [check-db-interceptor trim-v active-project]
  (fn [{db :db} [rect active-project]]
    (let [aspect-ratio (or
                         (get-in db [:camera :aspect-ratio])
                         (get-in db [:viewport :size]))
          poster-rect [(get-in active-project [:dimensions :width])
                       (get-in active-project [:dimensions :height])]
          rect-center (geometry/rect-center rect)
          rect [(:width rect) (:height rect)]
          new-scale (rect-scale rect poster-rect aspect-ratio)

          old-scale (get-in db [:camera :scale])
          c0 (get-in db [:camera :position])
          c1 [(:x rect-center) (:y rect-center)]
          w0 (* (first poster-rect) old-scale)
          w1 (* (first poster-rect) new-scale)
          [duration easing](anim/smooth-efficient c0 w0 c1 w1 (first poster-rect) 1 1.42)]
      {:animate-db [{:id :camera/position
                     :path [:camera :position]
                     :from (get-in db [:camera :position])
                     :to [(:x rect-center) (:y rect-center)]
                     :duration duration
                     :easing (fn []
                               (fn [t]
                                 (let [[pos _] (easing t)]
                                   pos)))}
                    {:id :camera/scale
                     :path [:camera :scale]
                     :from (get-in db [:camera :scale])
                     :to new-scale
                     :duration duration
                     :easing (fn []
                               (fn [t]
                                 (let [[_ scale] (easing t)]
                                   scale)))}]})))
