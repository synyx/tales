(ns tales.keybind
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch reg-event-fx trim-v]]
            [tales.util.events :as events]))

(defonce keydown-handler
  (events/on "keydown" #(dispatch [:key/down %])))

(defn cord [ev]
  (let [key (events/key-val ev)
        ctrl-key? (events/ctrl-key? ev)
        alt-key? (events/alt-key? ev)
        shift-key? (events/shift-key? ev)
        meta-key? (events/meta-key? ev)]
    (string/join "+" (filter some? [(if ctrl-key? "Ctrl")
                                    (if alt-key? "Alt")
                                    (if shift-key? "Shift")
                                    (if meta-key? "Meta")
                                    key]))))

(reg-event-fx :key/down
  [trim-v]
  (fn [_ [ev]]
    (let [action (case (cord ev)
                   "Enter" :slide/fly-to
                   " " :slide/next
                   "ArrowLeft" :slide/prev
                   "ArrowRight" :slide/next
                   "Ctrl+ArrowLeft" :slide/swap-prev
                   "Ctrl+ArrowRight" :slide/swap-next
                   "Meta+ArrowLeft" :slide/swap-prev
                   "Meta+ArrowRight" :slide/swap-next
                   nil)]
      (if action
        {:dispatch [action]}))))
