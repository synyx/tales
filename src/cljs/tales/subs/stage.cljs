(ns tales.subs.stage
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :stage/ready?
  (fn [db _]
    (let [dom-node (get-in db [:stage :dom-node])
          position (get-in db [:stage :position])]
      (and dom-node position))))

(reg-sub :stage/zoom
  (fn [db _]
    (get-in db [:stage :zoom])))

(reg-sub :stage/scale
  :<- [:stage/zoom]
  (fn [zoom _]
    (Math/pow 2 zoom)))

(reg-sub :stage/position
  (fn [db _]
    (get-in db [:stage :position])))