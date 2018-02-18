(ns tales.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary :refer [defroute] :include-macros true]
            [accountant.core :as accountant]))

(defroute home-path "/" []
          (dispatch [:set-active-project nil]))

(defroute editor-path "/editor/:slug/" [slug]
          (dispatch [:set-active-project slug]))

(defn init! []
  (accountant/configure-navigation! {:nav-handler  #(secretary/dispatch! %)
                                     :path-exists? #(secretary/locate-route %)})
  (accountant/dispatch-current!))
