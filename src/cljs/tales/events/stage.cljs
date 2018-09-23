(ns tales.events.stage
  (:require [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project]]
            [tales.dom :as dom]
            [tales.geometry :as geometry]))

(defn- zoom-around-point [position point old-zoom new-zoom]
  (let [scale (/ (geometry/zoom->scale old-zoom)
                (geometry/zoom->scale new-zoom))]
    (geometry/add-points
      (geometry/unscale position scale)
      (geometry/unscale point (- 1 scale)))))

(reg-event-fx :stage/mounted
  [trim-v active-project]
  (fn [{db :db} [dom-node active-project]]
    (let [dimensions (:dimensions active-project)
          position (get-in db [:stage :position])]
      {:db (assoc-in db [:stage :dom-node] dom-node)
       :dispatch-n [(when (nil? position) [:stage/fit-rect dimensions])]})))

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
    (let [current-position (get-in db [:stage :position])
          new-position (geometry/move-point current-position dx dy)]
      (assoc-in db [:stage :position] new-position))))

(reg-event-db :stage/zoom
  [trim-v]
  (fn [db [zoom]]
    (assoc-in db [:stage :zoom] zoom)))

(reg-event-db :stage/zoom-in
  [trim-v]
  (fn [db [delta]]
    (let [delta (or delta 1)
          current-zoom (get-in db [:stage :zoom])
          zoom (+ current-zoom delta)]
      (assoc-in db [:stage :zoom] zoom))))

(reg-event-db :stage/zoom-out
  [trim-v]
  (fn [db [delta]]
    (let [delta (or delta 1)
          current-zoom (get-in db [:stage :zoom])
          zoom (- current-zoom delta)]
      (assoc-in db [:stage :zoom] zoom))))

(reg-event-db :stage/zoom-around
  [trim-v]
  (fn [db [zoom point]]
    (let [orig-position (get-in db [:stage :position])
          orig-zoom (get-in db [:stage :zoom])
          new-position (zoom-around-point orig-position point orig-zoom zoom)]
      (-> db
        (assoc-in [:stage :position] new-position)
        (assoc-in [:stage :zoom] zoom)))))

(reg-event-db :stage/zoom-in-around
  [trim-v]
  (fn [db [point delta]]
    (let [orig-position (get-in db [:stage :position])
          orig-zoom (get-in db [:stage :zoom])
          delta (or delta 1)
          zoom (+ orig-zoom delta)
          new-position (zoom-around-point orig-position point orig-zoom zoom)]
      (-> db
        (assoc-in [:stage :position] new-position)
        (assoc-in [:stage :zoom] zoom)))))

(reg-event-db :stage/zoom-out-around
  [trim-v]
  (fn [db [point delta]]
    (let [orig-position (get-in db [:stage :position])
          orig-zoom (get-in db [:stage :zoom])
          delta (or delta 1)
          zoom (- orig-zoom delta)
          new-position (zoom-around-point orig-position point orig-zoom zoom)]
      (-> db
        (assoc-in [:stage :position] new-position)
        (assoc-in [:stage :zoom] zoom)))))

(reg-event-db :stage/fit-rect
  [trim-v]
  (fn [db [rect]]
    (let [dom-node (get-in db [:stage :dom-node])
          center (geometry/rect-center rect)
          zoom (->
                 (dom/client-size dom-node)
                 (geometry/rect-scale rect)
                 (geometry/scale->zoom))]
      (if dom-node
        (-> db
          (assoc-in [:stage :position] center)
          (assoc-in [:stage :zoom] zoom))
        db))))
