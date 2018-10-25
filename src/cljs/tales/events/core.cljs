(ns tales.events.core
  (:require [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.db :as db]
            [tales.interceptors :refer [active-project]]
            [tales.events.project]
            [tales.events.slide]
            [tales.events.view]))

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
