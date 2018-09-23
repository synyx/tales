(ns tales.events.editor
  (:require [re-frame.core :refer [reg-event-fx trim-v]]
            [tales.interceptors :refer [active-project]]))

(defn drop-nth [n coll]
  (concat
    (take n coll)
    (drop (inc n) coll)))

(reg-event-fx :editor/add-slide
  [trim-v active-project]
  (fn [_ [slide active-project]]
    (let [slides (:slides active-project)]
      {:dispatch [:project/update
                  (assoc active-project
                    :slides (conj slides slide))]})))

(reg-event-fx :editor/update-slide
  [trim-v active-project]
  (fn [{db :db} [slide active-project]]
    (let [slides (:slides active-project)
          idx (:active-slide db)]
      (if-not (nil? idx)
        {:dispatch [:project/update
                    (assoc active-project
                      :slides (assoc slides idx slide))]}))))

(reg-event-fx :editor/delete-active-slide
  [trim-v active-project]
  (fn [{db :db} [active-project]]
    (let [slides (:slides active-project)
          idx (:active-slide db)]
      (if-not (nil? idx)
        {:dispatch [:project/update
                    (assoc active-project
                      :slides (drop-nth idx slides))]}))))