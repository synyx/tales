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
    (get-in editor [:draw :slide])))

(reg-sub :current-slide
  :<- [:editor]
  (fn [editor _]
    (get editor :current-slide)))
