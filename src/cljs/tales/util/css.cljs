(ns tales.util.css)

(defn transform-origin [offset-x offset-y]
  (str offset-x "px " offset-y "px"))

(defn transform-matrix [a b c d tx ty]
  (str "matrix(" a "," b "," c "," d "," tx "," ty ")"))