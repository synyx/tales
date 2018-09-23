(ns tales.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(secretary/defroute home-path "/" []
  (dispatch [:activate-page :home]))

(secretary/defroute editor-path "/editor/:slug/" [slug]
  (do (dispatch [:activate-page :editor])
      (dispatch [:activate-project slug])))

(defn init! []
  (accountant/configure-navigation! {:nav-handler #(secretary/dispatch! %)
                                     :path-exists? #(secretary/locate-route %)})
  (accountant/dispatch-current!))
