(ns tales.subs.slide
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :slides
  :<- [:active-project]
  (fn [project _]
    (doall
      (map-indexed
        (fn [index slide] (assoc slide :index index))
        (:slides project)))))

(reg-sub :slide/active
  (fn [db _]
    (:active-slide db)))