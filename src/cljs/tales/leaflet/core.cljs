(ns tales.leaflet.core)

(defn map [dom-node options]
  (.map js/L dom-node (clj->js options)))

(defn image-overlay [file-path bounds]
  (.imageOverlay js/L file-path (clj->js bounds)))

(defn fit-bounds [map bounds]
  (.fitBounds map (clj->js bounds)))

(defn add-layer [map layer]
  (.addLayer map layer))
