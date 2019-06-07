(ns tales.db
  (:require [cljs.spec.alpha :as s]))

(defn- ^boolean real?
  "Returns true if `x` is a real number"
  [x] (and (number? x) (js/isFinite x)))

(s/def ::vec2 (s/tuple real? real?))
(s/def ::point ::vec2)

(s/def ::position ::point)
(s/def ::scale (s/and real? pos?))
(s/def ::camera (s/keys :req-un [::position ::scale]))

(s/def ::size (s/nilable ::vec2))
(s/def ::viewport (s/keys :req-un [::size]))

(s/def ::db (s/keys :req-un [::camera ::viewport]))

(def default-db
  {:projects {}
   :project nil
   :active-page nil
   :active-slide nil
   :camera {:position [0 0]
            :scale 1}
   :viewport {:size nil}
   :tick {:handlers {} :paused? true}
   :keybindings {:editor {"Enter" :slide/fly-to
                          " " :slide/next
                          "ArrowLeft" :slide/prev
                          "ArrowRight" :slide/next
                          "PageUp" :slide/prev
                          "PageDown" :slide/next
                          "Ctrl+ArrowLeft" :slide/swap-prev
                          "Ctrl+ArrowRight" :slide/swap-next
                          "Meta+ArrowLeft" :slide/swap-prev
                          "Meta+ArrowRight" :slide/swap-next
                          "Delete" :slide/delete}
                 :presenter {" " :slide/fly-to-next
                             "ArrowLeft" :slide/fly-to-prev
                             "ArrowRight" :slide/fly-to-next
                             "PageUp" :slide/fly-to-prev
                             "PageDown" :slide/fly-to-next}}})
