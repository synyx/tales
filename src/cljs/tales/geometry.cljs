(ns tales.geometry)

(defn zoom->scale [zoom]
  (Math/pow 2 zoom))

(defn scale->zoom [scale]
  (/ (Math/log scale) Math/LN2))

(defn scale [point scale]
  {:x (/ (:x point) scale)
   :y (/ (:y point) scale)})

(defn unscale [point scale]
  {:x (* (:x point) scale)
   :y (* (:y point) scale)})

(defn distance [p1 p2]
  {:x (- (:x p2) (:x p1))
   :y (- (:y p2) (:y p1))})

(defn rect-center [rect]
  {:x (+ (:x rect) (/ (:width rect) 2))
   :y (+ (:y rect) (/ (:height rect) 2))})

(defn move-point [point dx dy]
  {:x (+ (:x point) dx)
   :y (+ (:y point) dy)})

(defn move-rect [rect dx dy]
  (merge rect (move-point rect dx dy)))

(defn normalize-rect [rect]
  {:x (if (< (:width rect) 0) (+ (:x rect) (:width rect)) (:x rect))
   :y (if (< (:height rect) 0) (+ (:y rect) (:height rect)) (:y rect))
   :width (Math/abs (:width rect))
   :height (Math/abs (:height rect))})

(defn resize-rect [rect corner dx dy]
  (let [delta (case corner
                :top-left {:dx dx :dy dy :dwidth (- dx) :dheight (- dy)}
                :top-right {:dx 0 :dy dy :dwidth (+ dx) :dheight (- dy)}
                :bottom-right {:dx 0 :dy 0 :dwidth (+ dx) :dheight (+ dy)}
                :bottom-left {:dx dx :dy 0 :dwidth (- dx) :dheight (+ dy)})]
    (normalize-rect
      {:x (+ (:x rect) (:dx delta))
       :y (+ (:y rect) (:dy delta))
       :width (+ (:width rect) (:dwidth delta))
       :height (+ (:height rect) (:dheight delta))})))

(defn rect-scale [rect target]
  (Math/min
    (/ (:width rect) (:width target))
    (/ (:height rect) (:height target))))