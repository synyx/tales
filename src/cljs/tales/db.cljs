(ns tales.db
  (:require [cljs.spec.alpha :as s]))

(defn- ^boolean real?
  "Returns true if `x` is a real number"
  [x] (and (number? x) (js/isFinite x)))

(s/def ::point (s/tuple real? real?))

(s/def ::aspect-ratio (s/nilable (s/tuple real? real?)))
(s/def ::position ::point)
(s/def ::scale (s/and real? pos?))
(s/def ::camera (s/keys :req-un [::aspect-ratio ::position ::scale]))

(s/def ::size (s/tuple real? real?))
(s/def ::viewport (s/keys :req-un [::size]))

(s/def ::db (s/keys :req-un [::camera ::viewport]))

(def default-db
  {:projects {}
   :active-page nil
   :active-project nil
   :active-slide nil
   :camera {:aspect-ratio nil
            :position [0 0]
            :scale 1}
   :viewport {:size nil}
   :tick {:handlers {} :paused? true}})
