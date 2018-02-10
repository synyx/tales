(ns tales.project
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [config.core :refer [env]]
            [tales.utility :refer [slugify]])
  (:import (java.io File)))

(def ^:dynamic *project-dir* (or
                               (env :project-dir)
                               (str/join (File/separator) [(System/getProperty "user.home") "Tales"])))

(defn- config-file [slug]
  (str/join (File/separator) [*project-dir* slug "config.edn"]))

(defn- load-project! [slug]
  (let [filename (config-file slug)
        project (edn/read-string (slurp filename))]
    (assoc project :slug slug)))

(defn- save-project! [slug project]
  (let [filename (config-file slug)]
    (io/make-parents filename)
    (spit filename (pr-str project))))

(defn- project? [slug]
  (.exists (io/file (str/join (File/separator) [*project-dir* slug "config.edn"]))))

(defn find-all []
  (mapv load-project!
        (reverse
          (filter project?
                  (mapv #(.getName %)
                        (filter #(.isDirectory %) (.listFiles (io/file *project-dir*))))))))

(defn create
  ([name] (create name (slugify name)))
  ([name slug]
   (let [tale {:name name}]
     (save-project! slug tale)
     (assoc tale :slug slug))))

(s/def ::name string?)

(s/def ::new-project (s/keys :req-un [::name]))

(s/def ::project (s/keys :req-un [::name]))
