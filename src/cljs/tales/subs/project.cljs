(ns tales.subs.project
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub-raw :project/all
  (fn [db _]
    (dispatch [:project/get-all])
    (reagent.ratom/make-reaction
      (fn [] (:projects @db)))))

(reg-sub :project
  (fn [db _]
    (get db :project)))