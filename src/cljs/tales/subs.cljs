(ns tales.subs
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :active-page
         (fn [db _]
           (:active-page db)))

(reg-sub-raw :projects
             (fn [db _]
               (dispatch [:get-projects])
               (reagent.ratom/make-reaction
                 (fn [] (:projects @db)))))
