(ns tales.async
  (:require [clojure.core.async :refer [chan close! put!]]))

(defn <<< [f & args]
  (let [c (chan)]
    (apply f (concat args [(fn [x]
                             (if (or (nil? x)
                                     (undefined? x))
                               (close! c)
                               (put! c x)))]))
    c))
