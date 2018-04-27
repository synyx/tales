(ns tales.effects
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch reg-fx]]
            [tales.image :as image]))

(reg-fx :navigate
  (fn [url]
    (accountant/navigate! url)))

(reg-fx :determine-image-dimensions
  (fn [{project :project file :file}]
    (image/dimensions file
      #(dispatch [:update-project (assoc project :dimensions %)]))))
