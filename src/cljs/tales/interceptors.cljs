(ns tales.interceptors
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :refer [->interceptor after]]))

(defn- check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-db-interceptor (after (partial check-and-throw :tales.db/db)))

(def active-project
  (->interceptor
    :id :active-project
    :before (fn [context]
              (let [db (get-in context [:coeffects :db])
                    event (get-in context [:coeffects :event])
                    project (:project db)]
                (assoc-in context [:coeffects :event] (conj event project))))))
