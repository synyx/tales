(ns tales.events.stage
  (:require [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project]]
            [tales.dom :as dom]
            [tales.geometry :as geometry]
            [tales.util.transform :as transform]))

(reg-event-db :stage/mounted
  [trim-v]
  (fn [db [dom-node]]
    (assoc-in db [:stage :dom-node] dom-node)))

(reg-event-db :stage/unmounted
  (fn [db]
    (assoc-in db [:stage :dom-node] nil)))

(reg-event-db :stage/move-to
  [trim-v]
  (fn [db [x y]]
    (assoc-in db [:stage :position] {:x x :y y})))

(reg-event-db :stage/move-by
  [trim-v]
  (fn [db [dx dy]]
    (let [old-position (get-in db [:stage :position])
          new-position (geometry/move-point old-position dx dy)]
      (assoc-in db [:stage :position] new-position))))

(reg-event-db :stage/zoom
  [trim-v]
  (fn [db [zoom]]
    (assoc-in db [:stage :zoom] zoom)))

(reg-event-fx :stage/zoom-in
  [trim-v]
  (fn [{db :db} [delta]]
    (let [delta (or delta 1)
          old-zoom (get-in db [:stage :zoom])
          new-zoom (+ old-zoom delta)]
      {:dispatch [:stage/zoom new-zoom]})))

(reg-event-fx :stage/zoom-out
  [trim-v]
  (fn [{db :db} [delta]]
    (let [delta (or delta 1)
          old-zoom (get-in db [:stage :zoom])
          new-zoom (- old-zoom delta)]
      {:dispatch [:stage/zoom new-zoom]})))

(reg-event-db :stage/zoom-around
  [trim-v]
  (fn [db [new-zoom point]]
    (let [old-scale (geometry/zoom->scale (get-in db [:stage :zoom]))
          old-position (get-in db [:stage :position])
          new-scale (geometry/zoom->scale new-zoom)
          new-position (-> old-position
                         (transform/scale old-scale new-scale)
                         (geometry/add-points
                           (transform/moved-by-scale
                             point old-scale new-scale)))]
      (-> db
        (assoc-in [:stage :position] new-position)
        (assoc-in [:stage :origin] point)
        (assoc-in [:stage :zoom] new-zoom)))))

(reg-event-fx :stage/zoom-in-around
  [trim-v]
  (fn [{db :db} [point delta]]
    (let [delta (or delta 1)
          old-zoom (get-in db [:stage :zoom])
          new-zoom (+ old-zoom delta)]
      {:dispatch [:stage/zoom-around new-zoom point]})))

(reg-event-fx :stage/zoom-out-around
  [trim-v]
  (fn [{db :db} [point delta]]
    (let [delta (or delta 1)
          old-zoom (get-in db [:stage :zoom])
          new-zoom (- old-zoom delta)]
      {:dispatch [:stage/zoom-around new-zoom point]})))

(reg-event-db :stage/fit-rect
  [trim-v]
  (fn [db [rect]]
    (let [dom-node (get-in db [:stage :dom-node])
          new-position (geometry/rect-center rect)
          new-zoom (->
                     (dom/client-size dom-node)
                     (geometry/rect-scale rect)
                     (geometry/scale->zoom))]
      (if dom-node
        (-> db
          (assoc-in [:stage :position] new-position)
          (assoc-in [:stage :origin] new-position)
          (assoc-in [:stage :zoom] new-zoom))
        db))))
