(ns tales.events
  (:require [re-frame.core :refer [reg-event-db]]
            [tales.db :as db]))

(reg-event-db :initialise-db
              (fn [_ _] db/default-db))

(reg-event-db :set-active-page
              (fn [db [_ active-page]]
                (assoc db :active-page active-page)))
