(ns tales.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf :refer [dispatch dispatch-sync subscribe]]
            [tales.effects]
            [tales.events.core]
            [tales.subs.core]
            [tales.routes :as routes]
            [tales.views.core :as views]))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-page] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialise-db])
  (routes/init!)
  (mount-root))
