(ns tales.animation)

(defn linear
  "Linearly interpolates between `a` and `b` relative to `t` in `duration`."
  [a b duration t]
  (let [t (/ t duration)
        c (- b a)]
    (cond
      (<= t 0) a
      (>= t 1) b
      :else (+ a (* c t)))))

(def ^:private easings {:linear linear})

(defn create-anim-fn [a b duration easing]
  "Creates a function `fn [t]` which returns a value for time `t` relative to
  `a`, `b` and `duration`."
  (if (fn? easing)
    (easing a b duration)
    (partial (get easings easing) a b duration)))
