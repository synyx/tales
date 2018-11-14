(ns tales.util.events)

(defn ctrl-key? [ev]
  "Indicates if the ctrl key was pressed when the event occured."
  (.-ctrlKey ev))

(defn alt-key? [ev]
  "Indicates if the alt key was pressed when the event occured."
  (.-altKey ev))

(defn shift-key? [ev]
  "Indicates if the shift key was pressed when the event occured."
  (.-shiftKey ev))

(defn meta-key? [ev]
  "Indicates if the meta key was pressed when the event occured."
  (.-metaKey ev))

(defn key-val [ev]
  "The value of the key pressed by the user."
  (.-key ev))

(defn client-coord [ev]
  "Provides the coordinate within the client area at which the event occurred."
  {:x (.-clientX ev) :y (.-clientY ev)})

(defn page-coord [ev]
  "Provides the coordinate within the the entire document at which the event
  occurred. This includes any portion of the document not currently visible."
  {:x (.-pageX ev) :y (.-pageY ev)})

(defn wheel-delta [ev]
  "Provides the wheel scroll amount for each axis."
  {:x (.-deltaX ev) :y (.-deltaY ev) :z (.-deltaZ ev)})

(defn prevent [ev]
  "Prevents default actions of the current event."
  (.preventDefault ev))

(defn stop [ev]
  "Prevents further propagation of the current event in the capturing and
  bubbling phases."
  (.stopPropagation ev))

(defn on
  "Sets up a `handler` that will be called whenever the specified event of
  `type` is delivered to the `target`"
  ([type handler] (on js/window type handler))
  ([target type handler] (.addEventListener target type handler)))

(defn off
  "Removes a `handler` that was setup for the specified event of `type` on the
  `target`"
  ([type handler] (off js/window type handler))
  ([target type handler] (.removeEventListener target type handler)))
