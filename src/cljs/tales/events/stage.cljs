(ns tales.events.stage
  (:require [re-frame.core :refer [subscribe reg-event-db reg-event-fx]]
            [tales.geometry :as slide]))

(defn zoom->scale [zoom]
  (Math/pow 2 zoom))

(defn scale->zoom [scale]
  (/ (Math/log scale) Math/LN2))

(reg-event-fx :stage/mounted
  (fn [{db :db} [_ dom-node]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          dimensions (:dimensions project)
          position (get-in db [:stage :position])]
      {:db (assoc-in db [:stage :dom-node] dom-node)
       :dispatch-n [(when (nil? position) [:stage/fit-rect dimensions])]})))

(reg-event-db :stage/unmounted
  (fn [db _]
    (-> db
      (assoc-in [:stage :dom-node] nil))))

(reg-event-fx :stage/zoom
  (fn [{db :db} [_ zoom position]]
    (let [current-zoom (get-in db [:stage :zoom])
          current-position (get-in db [:stage :position])
          s1 (zoom->scale current-zoom)
          s2 (zoom->scale zoom)
          x1 (- (:x position) (:x current-position))
          y1 (- (:y position) (:y current-position))
          x2 (/ (* x1 s1) s2)
          y2 (/ (* y1 s1) s2)
          dx (- x2 x1)
          dy (- y2 y1)]
      {:db (assoc-in db [:stage :zoom] zoom)
       :dispatch [:stage/move-by (- dx) (- dy)]})))

(reg-event-fx :stage/zoom-in
  (fn [{db :db} [_ position]]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (+ current-zoom 1) position]})))

(reg-event-fx :stage/zoom-out
  (fn [{db :db} [_ position]]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (- current-zoom 1) position]})))

(reg-event-db :stage/move-to
  (fn [db [_ x y]]
    (assoc-in db [:stage :position] {:x x :y y})))

(reg-event-db :stage/move-by
  (fn [db [_ dx dy]]
    (let [current-position (get-in db [:stage :position])
          x (+ (:x current-position) dx)
          y (+ (:y current-position) dy)]
      (assoc-in db [:stage :position] {:x x :y y}))))

(reg-event-db :stage/fit-rect
  (fn [db [_ rect]]
    (let [center (slide/center rect)
          dom-node (get-in db [:stage :dom-node])
          sx (/ (.-clientWidth dom-node) (:width rect))
          sy (/ (.-clientHeight dom-node) (:height rect))
          scale (Math/min sx sy)
          zoom (scale->zoom scale)]
      (if dom-node
        (-> db
          (assoc-in [:stage :position] center)
          (assoc-in [:stage :zoom] zoom))
        db))))
