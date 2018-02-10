(ns tales.utility
  (:require [clojure.java.io :as io]
            [clojure.string :as s])
  (:import [java.text Normalizer Normalizer$Form]))

(defn- truncate [value max-length]
  (apply str (take max-length value)))

(defn- normalize [value]
  (let [normalized (Normalizer/normalize value Normalizer$Form/NFD)
        ascii (s/replace normalized #"[\P{ASCII}]+" "")]
    (s/trim (s/lower-case ascii))))

(defn slugify
  ([value] (slugify value 250))
  ([value max-length]
   (let [normalized (normalize value)
         slugified (s/replace normalized #"[\p{Space}\p{P}]+" "-")]
     (truncate slugified max-length))))

(defn delete-recursively [f]
  (let [file (io/file f)]
    (if (.exists file)
      (doseq [f (reverse (file-seq (io/file f)))]
        (io/delete-file f)))))
