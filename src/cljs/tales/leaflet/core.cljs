(ns tales.leaflet.core)

(defn create-map
  ([dom-node] (create-map dom-node {}))
  ([dom-node options] (.map js/L dom-node (clj->js options))))

(defn create-image-overlay
  ([file-path bounds] (create-image-overlay file-path bounds {}))
  ([file-path bounds options] (.imageOverlay js/L file-path (clj->js bounds) (clj->js options))))

(defn create-layer-group
  ([] (create-layer-group {}))
  ([options] (.layerGroup js/L (clj->js options))))

(defn create-feature-group
  ([] (create-feature-group {}))
  ([options] (.featureGroup js/L (clj->js options))))

(defn create-rectangle
  ([bounds] (create-rectangle bounds {}))
  ([bounds options] (.rectangle js/L (clj->js bounds) (clj->js options))))

(defn add-layer [map layer]
  (.addLayer map layer))

(defn has-layer [map layer]
  (.hasLayer map layer))

(defn clear-layers [layer]
  (.clearLayers layer))

(defn fly-to-bounds
  ([map bounds] (fly-to-bounds map bounds {}))
  ([map bounds options] (.flyToBounds map (clj->js bounds) (clj->js options))))

(defn set-bounds [layer bounds]
  (.setBounds layer (clj->js bounds)))

(defn fit-bounds [map bounds]
  (.fitBounds map (clj->js bounds)))

(defn on [container event-name f]
  (.on container event-name f))
