(ns tales.util.css)

(defn transform-matrix
  ([[m00 m01 m02 m10 m11 m12]]
   (transform-matrix m00 m10 m01 m11 m02 m12))
  ([m00 m10 m01 m11 m02 m12]
   (str "matrix(" m00 "," m10 "," m01 "," m11 "," m02 "," m12 ")")))

(defn transform-matrix3d
  ([[m00 m01 m02 m03 m10 m11 m12 m13 m20 m21 m22 m23 m30 m31 m32 m33]]
   (str "matrix3d("
     m00 "," m01 "," m02 "," m03 ","
     m10 "," m11 "," m12 "," m13 ","
     m20 "," m21 "," m22 "," m23 ","
     m30 "," m31 "," m32 "," m33 ")")))
