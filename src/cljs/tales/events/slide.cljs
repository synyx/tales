(ns tales.events.slide
  (:require [re-frame.core :refer [reg-event-db reg-event-fx trim-v]]))

(defn- drop-nth [n coll]
  (concat
    (take n coll)
    (drop (inc n) coll)))

(defn- swap [v i1 i2]
  (assoc v i2 (v i1) i1 (v i2)))

(reg-event-fx :slide/add
  [trim-v]
  (fn [{db :db} [slide]]
    (let [project (:project db)
          slides (:slides project)]
      {:dispatch [:project/update
                  (assoc project
                    :slides (conj slides slide))]})))

(reg-event-fx :slide/update
  [trim-v]
  (fn [{db :db} [slide]]
    (let [project (:project db)
          slides (:slides project)
          idx (:active-slide db)]
      (if-not (nil? idx)
        {:dispatch [:project/update
                    (assoc project
                      :slides (assoc slides idx slide))]}))))

(reg-event-fx :slide/delete
  [trim-v]
  (fn [{db :db} [idx]]
    (let [project (:project db)
          slides (:slides project)]
      (if-not (nil? idx)
        {:dispatch [:project/update
                    (assoc project
                      :slides (drop-nth idx slides))]}))))

(reg-event-db :slide/activate
  [trim-v]
  (fn [db [idx]]
    (assoc db :active-slide idx)))

(reg-event-fx :slide/next
  [trim-v]
  (fn [{db :db} _]
    (let [project (:project db)
          slides (:slides project)
          idx (or (:active-slide db) 0)]
      {:dispatch [:slide/activate (mod (+ idx 1) (count slides))]})))

(reg-event-fx :slide/prev
  [trim-v]
  (fn [{db :db} _]
    (let [project (:project db)
          slides (:slides project)
          idx (or (:active-slide db) 0)]
      {:dispatch [:slide/activate (mod (- idx 1) (count slides))]})))

(reg-event-fx :slide/swap
  [trim-v]
  (fn [{db :db} [idx1 idx2]]
    (let [project (:project db)
          slides (:slides project)
          slides-count (- (count slides) 1)]
      (if (and (<= 0 idx1 slides-count) (<= 0 idx2 slides-count))
        {:db (assoc db :active-slide idx2)
         :dispatch [:project/update
                    (assoc-in project [:slides]
                      (swap slides idx1 idx2))]}))))

(reg-event-fx :slide/swap-next
  [trim-v]
  (fn [{db :db} [idx]]
    (let [idx (or idx (:active-slide db))]
      {:dispatch [:slide/swap idx (+ idx 1)]})))

(reg-event-fx :slide/swap-prev
  [trim-v]
  (fn [{db :db} [idx]]
    (let [idx (or idx (:active-slide db))]
      {:dispatch [:slide/swap idx (- idx 1)]})))
