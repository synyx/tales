(ns tales.util.dom)

(defn element-by-id [id]
  "Returns the element whose id property matches the provided `id`."
  (.getElementById js/document id))

(defn bounding-rect [el]
  "Returns the bounding client rect of `el`."
  (.getBoundingClientRect el))

(defn size [el]
  "Provides the inner size of `el` in pixels."
  (let [rect (bounding-rect el)]
    [(.-width rect) (.-height rect)]))

(defn width [el]
  "Provides the inner width of `el` in pixels."
  (first (size el)))

(defn height [el]
  "Provides the inner height of `el` in pixels."
  (second (size el)))

(defn offset [el]
  "Provides the position of `el` relative to the viewport."
  (let [rect (bounding-rect el)]
    {:x (.-left rect) :y (.-top rect)}))