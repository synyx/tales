(ns tales.subs
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub-raw :projects
  (fn [db _]
    (dispatch [:get-projects])
    (reagent.ratom/make-reaction
      (fn [] (:projects @db)))))

(reg-sub :editor
  (fn [db _]
    (:editor db)))

(reg-sub :active-project
  :<- [:projects]
  :<- [:editor]
  (fn [[projects editor] _]
    (if-let [project (:project editor)]
      (get projects project))))

(reg-sub :slides
  :<- [:active-project]
  (fn [project _]
    (:slides project)))

(reg-sub :drawing?
  :<- [:editor]
  (fn [editor _]
    (:drawing? editor)))

(reg-sub :draw-rect
  :<- [:editor]
  (fn [editor _]
    (get-in editor [:draw :rect])))
