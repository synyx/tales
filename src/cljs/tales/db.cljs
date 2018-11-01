(ns tales.db
  (:require [cljs.spec.alpha :as s]))

(defn- ^boolean real?
  "Returns true if `x` is a real number"
  [x] (and (number? x) (js/isFinite x)))

(s/def ::vec2 (s/tuple real? real?))
(s/def ::point ::vec2)

(s/def ::aspect-ratio (s/nilable ::vec2))
(s/def ::position ::point)
(s/def ::scale (s/and real? pos?))
(s/def ::camera (s/keys :req-un [::aspect-ratio ::position ::scale]))

(s/def ::size (s/nilable ::vec2))
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
