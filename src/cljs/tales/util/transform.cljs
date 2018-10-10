(ns tales.util.transform)

(defn scale
  "Applies `new-scale` to point, assuming the original scale is 1 or
  `old-scale`."
  ([point new-scale] (scale point 1 new-scale))
  ([point old-scale new-scale]
   (let [f (/ old-scale new-scale)]
     {:x (* (:x point) f)
      :y (* (:y point) f)})))

(defn moved-by-origin [origin scale]
  "Returns how much a point moves when `scale` is applied around `origin`."
  {:x (- (/ (:x origin) scale) (:x origin))
   :y (- (/ (:y origin) scale) (:y origin))})

(defn moved-by-scale [point old-scale new-scale]
  "Returns how much a point at `old-scale` moves when applying `new-scale`."
  (let [scaled-point (scale point old-scale new-scale)]
    {:x (- (:x point) (:x scaled-point))
     :y (- (:y point) (:y scaled-point))}))