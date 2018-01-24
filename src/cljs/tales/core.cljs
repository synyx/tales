(ns tales.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

;; -------------------------
;; Views

(defn editor-page []
  [:div [:h2 "Welcome to tales"]
   [:div [:a {:href "#tell"} "go tell your tale"]]])

(defn tell-page []
  [:div [:h2 "Now tell your tale..."]
   [:div [:a {:href "#"} "go to the editor"]]])

;; -------------------------
;; Routes

(defonce page (atom #'editor-page))

(defn current-page []
  [:div [@page]])

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (reset! page #'editor-page))

(secretary/defroute "/tell" []
                    (reset! page #'tell-page))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
