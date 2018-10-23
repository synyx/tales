(ns tales.db
  (:require [cljs.spec.alpha :as s]))

(defn- ^boolean real?
  "Returns true if `x` is a real number"
  [x] (and (number? x) (js/isFinite x)))

(s/def ::x real?)
(s/def ::y real?)
(s/def ::point (s/keys :req-un [::x ::y]))

(s/def ::size (s/tuple real? real?))
(s/def ::zoom (s/and real?))
(s/def ::position ::point)
(s/def ::origin ::point)

(s/def ::stage (s/keys :req-un [::size ::zoom ::position ::origin]))

(s/def ::db (s/keys :req-un [::stage]))

(def default-db
  {:projects {}
   :active-page nil
   :active-project nil
   :active-slide nil
   :editor {}
   :stage {:size [0 0]
           :zoom 0
           :position {:x 0 :y 0}
           :origin {:x 0 :y 0}}})
