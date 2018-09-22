(ns tales.events.editor
  (:require [re-frame.core :refer [subscribe reg-event-db reg-event-fx]]))

(defn drop-nth [n coll]
  (concat
    (take n coll)
    (drop (inc n) coll)))

(reg-event-fx :editor/add-slide
  (fn [{db :db} [_ slide]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)]
      {:dispatch [:project/update
                  (assoc-in project [:slides] (conj slides slide))]})))

(reg-event-fx :editor/update-slide
  (fn [{db :db} [_ slide]]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          active-slide (:active-slide db)]
      {:dispatch [:project/update
                  (assoc-in project [:slides] (assoc slides active-slide slide))]})))

(reg-event-fx :editor/delete-active-slide
  (fn [{db :db}]
    (let [slug (:active-project db)
          project (get-in db [:projects slug])
          slides (:slides project)
          active-slide (:active-slide db)]
      (if-not (nil? active-slide)
        {:dispatch [:project/update
                    (assoc-in project [:slides] (drop-nth active-slide slides))]}))))