(ns tales.events.stage
  (:require [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project]]
            [tales.dom :as dom]
            [tales.geometry :as geometry]))

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

(reg-event-fx :stage/zoom
  [trim-v]
  (fn [{db :db} [zoom position]]
    (let [current-zoom (get-in db [:stage :zoom])
          current-position (get-in db [:stage :position])
          s1 (geometry/zoom->scale current-zoom)
          s2 (geometry/zoom->scale zoom)
          p1 (geometry/distance current-position position)
          p2 (geometry/scale (geometry/unscale p1 s1) s2)
          distance (geometry/distance p1 p2)]
      {:db (assoc-in db [:stage :zoom] zoom)
       :dispatch [:stage/move-by (- (:x distance)) (- (:y distance))]})))

(reg-event-fx :stage/zoom-in
  [trim-v]
  (fn [{db :db} [position]]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (+ current-zoom 1) position]})))

(reg-event-fx :stage/zoom-out
  [trim-v]
  (fn [{db :db} [position]]
    (let [current-zoom (get-in db [:stage :zoom])]
      {:dispatch [:stage/zoom (- current-zoom 1) position]})))

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
