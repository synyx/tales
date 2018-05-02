(ns tales.leaflet.helper)

(defn latlng-to-vec [latlng]
  [(.-lat latlng) (.-lng latlng)])

(defn latlng-bounds-to-vec [latlng-bounds]
  [(latlng-to-vec (.getNorthWest latlng-bounds))
   (latlng-to-vec (.getSouthEast latlng-bounds))])

(defn- bounds [image-dimensions]
  [[0 0] [(:height image-dimensions) (:width image-dimensions)]])

(defn- bounds->map [bounds]
  (let [x1 (first (first bounds))
        y1 (second (first bounds))
        x2 (first (second bounds))
        y2 (second (second bounds))]
    {:x1 x1 :y1 y1 :x2 x2 :y2 y2}))

(defn- normalize-bounds [bounds]
  (let [x1 (Math/min (second (first bounds)) (second (second bounds)))
        y1 (Math/min (first (first bounds)) (first (second bounds)))
        x2 (Math/max (second (first bounds)) (second (second bounds)))
        y2 (Math/max (first (first bounds)) (first (second bounds)))]
    [[x1 y1] [x2 y2]]))
