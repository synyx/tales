(ns tales.animation
  (:require [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as gv]
            [thi.ng.math.core :as m]))

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

(defn- cosh [x] (/ (+ (Math/pow Math/E x) (Math/pow Math/E (- x))) 2))
(defn- sinh [x] (/ (- (Math/pow Math/E x) (Math/pow Math/E (- x))) 2))
(defn- tanh [x] (/ (sinh x) (cosh x)))

(defn- b [w0 w1 u1 rho i]
  (let [w (if (= i 1) w1 w0)
        n1 (- (Math/pow w1 2) (Math/pow w0 2))
        n2 (* (Math/pow -1 i) (Math/pow rho 4) (Math/pow u1 2))
        d (* 2 w u1 (Math/pow rho 2))]
    (/ (+ n1 n2) d)))

(defn- r [b]
  (Math/log (- (Math/sqrt (+ (Math/pow b 2) 1)) b)))

(defn- S [r0 r1 rho]
  (/ (- r1 r0) rho))

(defn- S= [w0 w1 rho]
  (/ (Math/abs (Math/log (/ w1 w0))) rho))

(defn- u [w0 r0 rho s]
  (let [a (* w0 (cosh r0) (tanh (+ (* rho s) r0)))
        b (* w0 (sinh r0))]
    (/ (- a b) (Math/pow rho 2))))

(defn- w [w0 r0 rho s]
  (/ (* w0 (cosh r0)) (cosh (+ (* rho s) r0))))

(defn- w= [w0 k rho s]
  (* w0 (Math/exp (* k rho s))))

(defn smooth-efficient [c0 w0 c1 w1 V rho]
  "Implementation of \"Smooth and efficient zooming and panning\",
  Jarke J. van Wijk and Wim A. A. Nuij, TU Eindhoven, Netherlands."
  (if (= c0 c1)
    (let [S (S= w0 w1 rho)
          duration (* 1000 S V)
          tick-fn (fn [t]
                    (let [t (/ t 1000)
                          s (Math/min (* V t) S)
                          k (if (< w1 w0) -1 1)
                          ws (w= w0 k rho s)]
                      [c0 ws]))]
      [duration tick-fn])
    (let [u1 (g/dist (gv/vec2 c0) c1)
          b0 (b w0 w1 u1 rho 0)
          b1 (b w0 w1 u1 rho 1)
          r0 (r b0)
          r1 (r b1)
          S (S r0 r1 rho)
          duration (* 1000 S V)
          tick-fn (fn [t]
                    (let [t (/ t 1000)
                          s (Math/min (* V t) S)
                          us (u w0 r0 rho s)
                          ws (w w0 r0 rho s)
                          pos (m/mix (gv/vec2 c0) c1 (/ us u1))]
                      [pos ws]))]
      [duration tick-fn])))
