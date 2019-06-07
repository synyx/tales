(ns tales.events.core
  (:require [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]
            [tales.db :as db]
            [tales.events.project]
            [tales.events.slide]
            [tales.events.view]))

(reg-event-db :initialise-db
  (fn [] db/default-db))

(reg-event-db :activate-page
  [trim-v]
  (fn [db [active-page]]
    (assoc db :active-page active-page)))
