(ns tales.views.presenter
  (:require [tales.routes :as routes]
            [tales.views.stage :refer [stage]]))

(defn page []
  [:div {:id "presenter"}
   [:main
    [stage]]])
