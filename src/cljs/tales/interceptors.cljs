(ns tales.interceptors
  (:require [re-frame.core :refer [->interceptor]]))

(def active-project
  (->interceptor
    :id :active-project
    :before (fn [context]
              (let [db (get-in context [:coeffects :db])
                    event (get-in context [:coeffects :event])
                    slug (:active-project db)
                    project (get-in db [:projects slug])]
                (assoc-in context [:coeffects :event] (conj event project))))))
