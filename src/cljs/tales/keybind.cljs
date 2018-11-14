(ns tales.keybind
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch reg-event-fx trim-v]]
            [tales.util.events :as events]))

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
  (fn [{db :db} [cord]]
    (let [page (:active-page db)
          action (get-in db [:keybindings page cord])]
      (when action
        (if (coll? action)
          {:dispatch-n (vec (map #(vec [%]) action))}
          {:dispatch [action]})))))

(defonce keydown-handler
  (events/on "keydown" #(dispatch [:key/down (cord %)])))
