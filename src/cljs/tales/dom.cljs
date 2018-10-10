(ns tales.dom
  (:require [tales.util.events :as events]))

(defn client-size [dom-node]
  {:width (.-clientWidth dom-node)
   :height (.-clientHeight dom-node)})

(defn screen-point->node-point [pos dom-node]
  (let [rect (.getBoundingClientRect dom-node)]
    {:x (- (:x pos) (.-left rect) (.-clientLeft dom-node))
     :y (- (:y pos) (.-top rect) (.-clientTop dom-node))}))

(defn- drag-move-fn [drag-start on-drag]
  (fn [ev]
    (let [drag-start @drag-start
          drag-end (events/client-coord ev)
          dx (- (:x drag-end) (:x drag-start))
          dy (- (:y drag-end) (:y drag-start))]
      (events/prevent ev)
      (on-drag {:start drag-start :end drag-end :dx dx :dy dy}))))

(defn- drag-end-fn [drag-move drag-end on-drag-end]
  (fn [ev]
    (events/prevent ev)
    (events/off "mousemove" drag-move)
    (events/off "mouseup" @drag-end)
    (on-drag-end)))

(defn dragging
  ([ev on-drag] (dragging ev on-drag (fn [])))
  ([ev on-drag on-drag-end]
   (let [drag-start (atom (events/client-coord ev))
         drag-move (drag-move-fn drag-start on-drag)
         drag-end-atom (atom nil)
         drag-end (drag-end-fn drag-move drag-end-atom on-drag-end)]
     (reset! drag-end-atom drag-end)
     (events/on "mousemove" drag-move)
     (events/on "mouseup" drag-end))))