(ns tales.events.core
  (:require [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.db :as db]
            [tales.interceptors :refer [active-project]]
            [tales.events.editor]
            [tales.events.project]
            [tales.events.stage]))

(defn swap [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(reg-event-db :initialise-db
  (fn [] db/default-db))

(reg-event-db :activate-page
  [trim-v]
  (fn [db [active-page]]
    (assoc db :active-page active-page)))

(reg-event-db :activate-project
  [trim-v]
  (fn [db [active-project]]
    (assoc db :active-project active-project)))

(reg-event-db :activate-slide
  [trim-v]
  (fn [db [idx]]
    (assoc db :active-slide idx)))

(reg-event-fx :next-slide
  [trim-v active-project]
  (fn [{db :db} [active-project]]
    (let [slides (:slides active-project)
          idx (or (:active-slide db) 0)]
      {:dispatch [:activate-slide (mod (+ idx 1) (count slides))]})))

(reg-event-fx :prev-slide
  [trim-v active-project]
  (fn [{db :db} [active-project]]
    (let [slides (:slides active-project)
          idx (or (:active-slide db) 0)]
      {:dispatch [:activate-slide (mod (- idx 1) (count slides))]})))

(reg-event-fx :change-order
  [trim-v active-project]
  (fn [{db :db} [delta active-project]]
    (let [slides (:slides active-project)
          active-slide (:active-slide db)
          next-slide (+ active-slide delta)]
      (if (<= 0 next-slide (- (count slides) 1))
        {:db (assoc db :active-slide next-slide)
         :dispatch [:project/update
                    (assoc-in active-project [:slides]
                      (swap slides active-slide next-slide))]}))))
