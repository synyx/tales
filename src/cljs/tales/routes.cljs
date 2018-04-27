(ns tales.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(secretary/defroute home-path "/" []
  (dispatch [:set-active-project nil]))

(secretary/defroute editor-path "/editor/:slug/" [slug]
  (dispatch [:set-active-project slug]))

(defn init! []
  (accountant/configure-navigation! {:nav-handler #(secretary/dispatch! %)
                                     :path-exists? #(secretary/locate-route %)})
  (accountant/dispatch-current!))
