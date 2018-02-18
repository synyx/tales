(ns tales.subs
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :active-project-slug
         (fn [db _]
           (:active-project db)))

(reg-sub :active-project
         :<- [:projects]
         :<- [:active-project-slug]
         (fn [[projects slug] _]
           (first (filter #(= (:slug %) slug) projects))))

(reg-sub-raw :projects
             (fn [db _]
               (dispatch [:get-projects])
               (reagent.ratom/make-reaction
                 (fn [] (:projects @db)))))
