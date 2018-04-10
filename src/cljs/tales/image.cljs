(ns tales.image
  (:require [clojure.core.async :refer [<! chan close! put! go]]
            [clojure.string :as str]))

(defn- dimensions-from-view-box [view-box]
  (let [view-box (str/split view-box #" ")]
    (if (= (count view-box) 4)
      {:width  (Math/round (- (nth view-box 2) (nth view-box 0)))
       :height (Math/round (- (nth view-box 3) (nth view-box 1)))}
      {:width nil :height nil})))

(defn- get-dom [svg-data cb]
  (let [doc (.parseFromString (js/DOMParser.) svg-data "image/svg+xml")]
    (cb (aget (.getElementsByTagName doc "svg") 0))))

(defn- with-svg [file cb]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) #(get-dom (-> % .-target .-result) cb))
    (.readAsText reader file)))

(defn- with-image [file cb]
  (let [reader (js/FileReader.)
        image  (js/Image.)]
    (set! (.-onload reader) #(set! (.-src image) (-> % .-target .-result)))
    (set! (.-onload image) #(cb image))
    (.readAsDataURL reader file)))

(defn- svg-dimensions [svg]
  (let [width  (.getAttribute svg "width")
        height (.getAttribute svg "height")]
    (if (and width height)
      {:width width :height height}
      (dimensions-from-view-box (.getAttribute svg "viewBox")))))

(defn- image-dimensions [image]
  (let [width  (.-width image)
        height (.-height image)]
    {:width width :height height}))

(defn dimensions [file cb]
  (let [type (.-type file)]
    (case type
      "image/svg+xml" (with-svg file #(cb (svg-dimensions %)))
      (with-image file #(cb (image-dimensions %))))))
