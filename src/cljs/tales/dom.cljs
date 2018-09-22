(ns tales.dom)

(defn ctrl-key? [e]
  (or
    (-> e .-ctrlKey)
    (-> e .-metaKey)))

(defn mouse-position [e]
  {:x (.-clientX e) :y (.-clientY e)})

(defn screen-point->container-point [pos dom-node]
  (let [rect (.getBoundingClientRect dom-node)]
    {:x (- (:x pos) (.-left rect) (.-clientLeft dom-node))
     :y (- (:y pos) (.-top rect) (.-clientTop dom-node))}))

(defn- drag-move-fn [drag-start on-drag]
  (fn [e]
    (let [drag-start @drag-start
          drag-end (mouse-position e)
          dx (- (:x drag-end) (:x drag-start))
          dy (- (:y drag-end) (:y drag-start))]
      (.preventDefault e)
      (on-drag {:start drag-start :end drag-end :dx dx :dy dy}))))

(defn- drag-end-fn [drag-move drag-end on-drag-end]
  (fn [e]
    (.preventDefault e)
    (.removeEventListener js/window "mousemove" drag-move)
    (.removeEventListener js/window "mouseup" @drag-end)
    (on-drag-end)))

(defn dragging
  ([e on-drag] (dragging e on-drag (fn [])))
  ([e on-drag on-drag-end]
   (let [drag-start (atom (mouse-position e))
         drag-move (drag-move-fn drag-start on-drag)
         drag-end-atom (atom nil)
         drag-end (drag-end-fn drag-move drag-end-atom on-drag-end)]
     (reset! drag-end-atom drag-end)
     (.addEventListener js/window "mousemove" drag-move)
     (.addEventListener js/window "mouseup" drag-end))))