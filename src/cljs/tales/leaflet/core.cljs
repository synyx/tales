(ns tales.leaflet.core)

(defn create-map
  ([dom-node] (create-map dom-node {}))
  ([dom-node options] (.map js/L dom-node (clj->js options))))

(defn create-image-overlay
  ([file-path bounds] (create-image-overlay file-path bounds {}))
  ([file-path bounds options] (.imageOverlay js/L
                                file-path
                                (clj->js bounds) (clj->js options))))

(defn create-layer-group
  ([] (create-layer-group {}))
  ([options] (.layerGroup js/L (clj->js options))))

(defn create-feature-group
  ([] (create-feature-group {}))
  ([options] (.featureGroup js/L (clj->js options))))

(defn create-rectangle
  ([bounds] (create-rectangle bounds {}))
  ([bounds options] (.rectangle js/L (clj->js bounds) (clj->js options))))

(defn create-div-icon
  ([] (create-div-icon {}))
  ([options] (.divIcon js/L (clj->js options))))

(defn create-marker
  ([latlng] (create-marker latlng {}))
  ([latlng options] (.marker js/L (clj->js latlng) (clj->js options))))

(defn add-layer [layer-container layer]
  (.addLayer layer-container layer))

(defn has-layer [layer-container layer]
  (.hasLayer layer-container layer))

(defn remove-layer [layer-container layer]
  (.removeLayer layer-container layer))

(defn clear-layers [layer]
  (.clearLayers layer))

(defn fly-to-bounds
  ([map bounds] (fly-to-bounds map bounds {}))
  ([map bounds options] (.flyToBounds map (clj->js bounds) (clj->js options))))

(defn set-bounds [layer bounds]
  (.setBounds layer (clj->js bounds)))

(defn set-latlng [layer latlng]
  (.setLatLng layer (clj->js latlng)))

(defn set-style [layer options]
  (.setStyle layer (clj->js options)))

(defn fit-bounds [map bounds]
  (.fitBounds map (clj->js bounds)))

(defn on [container event-name f]
  (.on container event-name f))

(defn off ([container event-name]
           (.off container event-name)))

(defn latlng->coord [latlng]
  {:x (.-lng latlng) :y (.-lat latlng)})

(defn coord->latlng [coord]
  (.latLng js/L (:y coord) (:x coord)))

(defn latlng-bounds->slide-rect [latlng-bounds]
  (let [bottom-left (latlng->coord (.getSouthWest latlng-bounds))
        top-right (latlng->coord (.getNorthEast latlng-bounds))]
    {:bottom-left bottom-left :top-right top-right}))

(defn slide-rect->latlng-bounds [slide-rect]
  (.latLngBounds js/L
    (coord->latlng slide-rect)
    (coord->latlng {:x (+ (:x slide-rect) (:width slide-rect))
                    :y (+ (:y slide-rect) (:height slide-rect))})))
