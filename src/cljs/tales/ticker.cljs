(ns tales.ticker
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch reg-event-db trim-v]]))

(defprotocol PTickHandler
  (init-state [_ db]
    "Called during :add-tick-handlers event handling. Must return updated db.")
  (tick [_ db]
    "Called at each tick with current app-db. Must return updated db."))

(defn- re-trigger-ticker
  "Dispatches :next-tick event at next redraw cycle."
  [] (r/next-tick (fn [] (dispatch [:next-tick]))))

(defn add-handler [id handler]
  (dispatch [:add-tick-handlers {id handler}]))

(defn remove-handler [id]
  (dispatch [:remove-tick-handlers [id]]))

(reg-event-db :add-tick-handlers
  [trim-v]
  (fn [db [handlers]]
    (let [db (reduce-kv
               (fn [db _ handler] (init-state handler db))
               (update-in db [:tick :handlers] merge handlers)
               handlers)
          paused? (= (count (get-in db [:tick :handlers])) 0)]
      (when-not paused? (re-trigger-ticker))
      (assoc-in db [:tick :paused?] paused?))))

(reg-event-db :remove-tick-handlers
  [trim-v]
  (fn [{ticker :tick :as db} [ids]]
    (let [handlers (apply dissoc (:handlers ticker) ids)]
      (-> db
        (assoc-in [:tick :handlers] handlers)
        (assoc-in [:tick :paused?] (= (count handlers) 0))))))

(reg-event-db :next-tick
  [trim-v]
  (fn [{ticker :tick :as db} _]
    (if-not (:paused? ticker)
      (do (re-trigger-ticker)
          (reduce-kv
            (fn [db _ handler] (tick handler db))
            db (:handlers ticker)))
      db)))
