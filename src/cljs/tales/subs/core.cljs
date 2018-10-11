(ns tales.subs.core
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]
            [tales.subs.project]
            [tales.subs.slide]
            [tales.subs.stage]))

(reg-sub :active-page
  (fn [db _]
    (:active-page db)))

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