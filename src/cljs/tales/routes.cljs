(ns tales.routes
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary :refer [defroute] :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
            (dispatch [:set-active-page :project-page]))

  (defroute "/editor/:slug" [slug]
            (do (dispatch [:set-active-page :editor-page])
                (dispatch [:set-active-project slug]))))

(defn init! []
  (app-routes)
  (hook-browser-navigation!))
