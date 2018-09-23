(ns tales.subs.core
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]
            [tales.subs.editor]
            [tales.subs.project]
            [tales.subs.stage]))

(reg-sub :active-page
  (fn [db _]
    (:active-page db)))

(reg-sub :active-project
  (fn [db _]
    (get-in db [:projects (:active-project db)])))

(reg-sub :active-slide
  (fn [db _]
    (:active-slide db)))

(reg-sub :poster/dimensions
  :<- [:active-project]
  (fn [project _]
    (:dimensions project)))

(reg-sub :poster/file-path
  :<- [:active-project]
  (fn [project _]
    (:file-path project)))

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
