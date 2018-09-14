(ns tales.subs
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :active-page
  (fn [db _]
    (:active-page db)))

(reg-sub-raw :projects
  (fn [db _]
    (dispatch [:get-projects])
    (reagent.ratom/make-reaction
      (fn [] (:projects @db)))))

(reg-sub :editor
  (fn [db _]
    (:editor db)))

(reg-sub :active-project
  (fn [db _]
    (get-in db [:projects (:active-project db)])))

(reg-sub :poster/dimensions
  :<- [:active-project]
  (fn [project _]
    (:dimensions project)))

(reg-sub :poster/file-path
  :<- [:active-project]
  (fn [project _]
    (:file-path project)))

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

(reg-sub :slides
  :<- [:active-project]
  (fn [project _]
    (doall
      (map-indexed
        (fn [index slide] (assoc slide :index index))
        (:slides project)))))

(reg-sub :slide
  :<- [:slides]
  (fn [slides [_ idx]]
    (nth slides idx)))

(reg-sub :navigator
  :<- [:editor]
  (fn [editor _]
    (:navigator editor)))

(reg-sub :drawing?
  :<- [:editor]
  (fn [editor _]
    (:drawing? editor)))

(reg-sub :draw-slide
  :<- [:editor]
  (fn [editor _]
    (let [rect (get-in editor [:draw :slide :rect])
          delta (or (get-in editor [:draw :delta])
                  {:dx 0 :dy 0 :dwidth 0 :dheight 0})]
      {:rect {:x (+ (:x rect) (:dx delta))
              :y (+ (:y rect) (:dy delta))
              :width (+ (:width rect) (:dwidth delta))
              :height (+ (:height rect) (:dheight delta))}})))

(reg-sub :current-slide
  :<- [:editor]
  (fn [editor _]
    (get editor :current-slide)))
