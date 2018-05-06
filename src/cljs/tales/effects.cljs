(ns tales.effects
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch reg-fx]]
            [tales.image :as image]
            [tales.leaflet.core :as L]))

(reg-fx :navigate
  (fn [url]
    (accountant/navigate! url)))

(reg-fx :determine-image-dimensions
  (fn [{project :project file :file}]
    (image/dimensions file
      #(dispatch [:update-project (assoc project :dimensions %)]))))

(reg-fx :navigator-fly-to
  (fn [[navigator slide]]
    (if-not (nil? navigator)
      (L/fly-to-bounds navigator
        (L/slide-rect->latlng-bounds (:rect slide))))))