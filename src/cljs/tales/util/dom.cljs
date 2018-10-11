(ns tales.util.dom)

(defn element-by-id [id]
  "Returns the element whose id property matches the provided `id`."
  (.getElementById js/document id))

(defn width [el]
  "Provides the inner width of `el` in pixels."
  (.-clientWidth el))

(defn height [el]
  "Provides the inner height of `el` in pixels."
  (.-clientHeight el))

(defn size [el]
  "Provides the inner size of `el` in pixels."
  {:width (width el) :height (height el)})

(defn offset [el]
  "Provides the position of `el` relative to the viewport."
  (let [rect (.getBoundingClientRect el)]
    {:x (.-left rect) :y (.-top rect)}))