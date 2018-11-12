(ns tales.events.view
  (:require [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.animation :as anim]
            [tales.interceptors :refer [check-db-interceptor]]
            [tales.geometry :as geometry]
            [thi.ng.math.core :as m]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.matrix :as gm]
            [thi.ng.geom.vector :as gv]))

(reg-event-db :viewport/set-size
  [check-db-interceptor trim-v]
  (fn [db [size]]
    (assoc-in db [:viewport :size] size)))

(reg-event-fx :camera/setup
  [trim-v]
  (fn [{db :db} _]
    (let [poster-dimensions (get-in db [:project :dimensions])]
      {:dispatch [:camera/fit-rect poster-dimensions]})))

(reg-event-db :camera/destroy
  [check-db-interceptor trim-v]
  (fn [db _]
    (dissoc db :camera)))

(reg-event-db :camera/move-to
  [check-db-interceptor trim-v]
  (fn [db [position]]
    (assoc-in db [:camera :position] position)))

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

(defn- rect-scale [[aw ah] viewport-size]
  (let [scale (-> {:size viewport-size}
                (gm/ortho)
                (g/transform-vector (gv/vec3 [aw ah] 0))
                (m/div 2))]
    (apply Math/max scale)))

(reg-event-db :camera/fit-rect
  [check-db-interceptor trim-v]
  (fn [db [rect]]
    (let [viewport-size (get-in db [:viewport :size])
          rect-center (geometry/rect-center rect)
          rect [(:width rect) (:height rect)]
          new-scale (rect-scale rect viewport-size)]
      (-> db
        (assoc-in [:camera :position] [(:x rect-center) (:y rect-center)])
        (assoc-in [:camera :scale] new-scale)))))

(reg-event-fx :camera/fly-to-rect
  [check-db-interceptor trim-v]
  (fn [{db :db} [rect]]
    (let [viewport-size (get-in db [:viewport :size])
          rect-center (geometry/rect-center rect)
          rect [(:width rect) (:height rect)]
          c0 (get-in db [:camera :position])
          w0 (get-in db [:camera :scale])
          c1 [(:x rect-center) (:y rect-center)]
          w1 (rect-scale rect viewport-size)
          [duration easing] (anim/smooth-efficient c0 w0 c1 w1 1 1.42)]
      {:animate-db [{:id :camera/position
                     :path [:camera :position]
                     :from c0
                     :to c1
                     :duration duration
                     :easing #(fn [t]
                                (let [[pos _] (easing t)]
                                  pos))}
                    {:id :camera/scale
                     :path [:camera :scale]
                     :from w0
                     :to w1
                     :duration duration
                     :easing #(fn [t]
                                (let [[_ scale] (easing t)]
                                  scale))}]})))
