(ns tales.util.async
  (:import [goog.async Debouncer]
           [goog.async Throttle]))

(defn debounce [f interval]
  "Call `f` exactly once for any number of repeatedly calls so long as it is
  called less than `interval` apart (in milliseconds)."
  (let [debouncer (Debouncer. f interval)]
    (fn [& args] (.apply (.-fire debouncer) debouncer (to-array args)))))