(ns tales.slide.core)

(defn center [rect]
  {:x (+ (:x rect) (/ (:width rect) 2))
   :y (+ (:y rect) (/ (:height rect) 2))})

(defn move [rect dx dy]
  {:x (+ (:x rect) dx)
   :y (+ (:y rect) dy)
   :width (:width rect)
   :height (:height rect)})

(defn normalize [rect]
  {:x (if (< (:width rect) 0) (+ (:x rect) (:width rect)) (:x rect))
   :y (if (< (:height rect) 0) (+ (:y rect) (:height rect)) (:y rect))
   :width (Math/abs (:width rect))
   :height (Math/abs (:height rect))})

(defn resize [rect corner dx dy]
  (let [delta (case corner
                :top-left {:dx dx :dy dy :dwidth (- dx) :dheight (- dy)}
                :top-right {:dx 0 :dy dy :dwidth (+ dx) :dheight (- dy)}
                :bottom-right {:dx 0 :dy 0 :dwidth (+ dx) :dheight (+ dy)}
                :bottom-left {:dx dx :dy 0 :dwidth (- dx) :dheight (+ dy)})]
    (normalize
      {:x (+ (:x rect) (:dx delta))
       :y (+ (:y rect) (:dy delta))
       :width (+ (:width rect) (:dwidth delta))
       :height (+ (:height rect) (:dheight delta))})))