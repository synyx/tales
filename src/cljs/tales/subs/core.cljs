(ns tales.subs.core
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]
            [tales.subs.project]
            [tales.subs.slide]
            [tales.subs.view]))

(reg-sub :active-page
  (fn [db _]
    (:active-page db)))

(reg-sub :poster/dimensions
  :<- [:project]
  (fn [project _]
    (:dimensions project)))

(reg-sub :poster/file-path
  :<- [:project]
  (fn [project _]
    (:file-path project)))
