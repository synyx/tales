(ns tales.util.transform)

(defn scale
  "Applies `new-scale` to point, assuming the original scale is 1 or
  `old-scale`."
  ([point new-scale] (scale point 1 new-scale))
  ([point old-scale new-scale]
   (let [f (/ old-scale new-scale)]
     {:x (* (:x point) f)
      :y (* (:y point) f)})))
