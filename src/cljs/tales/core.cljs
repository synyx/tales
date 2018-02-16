(ns tales.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame :refer [dispatch dispatch-sync]]
            [tales.events]
            [tales.routes :as routes]
            [tales.subs]
            [tales.views :as views]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-page] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialise-db])
  (routes/init!)
  (mount-root))
