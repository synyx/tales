(ns tales.db
  (:require [cljs.spec.alpha :as s]))

(defn- ^boolean real?
  "Returns true if `x` is a real number"
  [x] (and (number? x) (js/isFinite x)))

(s/def ::point (s/tuple real? real?))

(s/def ::scale (s/and real? pos?))
(s/def ::size (s/tuple real? real?))
(s/def ::position ::point)

(s/def ::stage (s/keys :req-un [::scale ::size ::position]))

(s/def ::db (s/keys :req-un [::stage]))

(def default-db
  {:projects {}
   :active-page nil
   :active-project nil
   :active-slide nil
   :editor {}
   :stage {:scale 1
           :size [0 0]
           :position [0 0]}})
