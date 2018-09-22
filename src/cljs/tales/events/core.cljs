(ns tales.events.core
  (:require [day8.re-frame.http-fx]
            [re-frame.core :refer [subscribe reg-event-db reg-event-fx]]
            [tales.db :as db]
            [tales.events.editor]
            [tales.events.project]
            [tales.events.stage]))

(defn swap [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(reg-event-db :initialise-db
  (fn [_ _] db/default-db))

(reg-event-db :set-active-page
  (fn [db [_ active-page]]
    (assoc db :active-page active-page)))

(reg-event-db :set-active-project
  (fn [db [_ active-project]]
    (assoc db :active-project active-project)))

(reg-event-fx :activate-slide
  (fn [{db :db} [_ idx]]
    {:db (assoc-in db [:editor :current-slide] idx)}))

(reg-event-fx :move-to-slide
  (fn [_ [_ idx]]
    (let [slide (subscribe [:slide idx])
          rect (:rect @slide)]
      {:dispatch [:stage/fit-rect rect]})))

(reg-event-fx :next-slide
  (fn [{db :db} _]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      (if (nil? current-slide)
        {:dispatch [:activate-slide 0]}
        {:dispatch [:activate-slide (mod (+ current-slide 1) (count slides))]}))))

(reg-event-fx :prev-slide
  (fn [{db :db} _]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])]
      (if (nil? current-slide)
        {:dispatch [:activate-slide 0]}
        {:dispatch [:activate-slide (mod (- current-slide 1) (count slides))]}))))

(reg-event-fx :change-order
  (fn [{db :db} [_ delta]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          current-slide (get-in db [:editor :current-slide])
          next-slide (+ current-slide delta)]
      (if (<= 0 next-slide (- (count slides) 1))
        {:db (assoc-in db [:editor :current-slide] next-slide)
         :dispatch [:project/update
                    (assoc-in project [:slides]
                      (swap slides current-slide next-slide))]}))))

