(ns tales.util.css)

(defn transform-origin [offset-x offset-y]
  (str offset-x "px " offset-y "px"))

(defn transform-matrix
  ([[m00 m01 m02 m10 m11 m12]]
   (transform-matrix m00 m10 m01 m11 m02 m12))
  ([m00 m10 m01 m11 m02 m12]
   (str "matrix(" m00 "," m10 "," m01 "," m11 "," m02 "," m12 ")")))