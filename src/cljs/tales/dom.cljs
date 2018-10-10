(ns tales.dom)

(defn client-size [dom-node]
  {:width (.-clientWidth dom-node)
   :height (.-clientHeight dom-node)})

(defn screen-point->node-point [pos dom-node]
  (let [rect (.getBoundingClientRect dom-node)]
    {:x (- (:x pos) (.-left rect) (.-clientLeft dom-node))
     :y (- (:y pos) (.-top rect) (.-clientTop dom-node))}))
