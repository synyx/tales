(ns tales.project
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [config.core :refer [env]]
            [me.raynes.fs :as fs]
            [tales.utility :refer [slugify]]))

(def ^:dynamic *project-dir* (or
                               (fs/file (env :project-dir))
                               (fs/file (fs/home) "Tales")))

(defn- config-file [slug]
  (fs/file *project-dir* slug "config.edn"))

(defn- load-project! [slug]
  (let [filename (config-file slug)
        project  (edn/read-string (slurp filename))]
    (assoc project :slug slug)))

(defn- save-project! [slug project]
  (let [filename (config-file slug)]
    (fs/mkdirs (fs/parent filename))
    (spit filename (pr-str project))))

(defn project? [slug]
  (fs/exists? (fs/file *project-dir* slug "config.edn")))

(defn find-all []
  (mapv load-project!
        (reverse
          (filter project?
                  (mapv fs/name
                        (filter fs/directory? (fs/list-dir *project-dir*)))))))

(defn find-by-slug [slug]
  (if (project? slug)
    (load-project! slug)))

(defn create
  ([name] (create name (slugify name)))
  ([name slug]
   (let [tale {:name name}]
     (save-project! slug tale)
     (assoc tale :slug slug))))

(defn delete [slug]
  (fs/delete-dir (fs/file *project-dir* slug)))

(s/def ::name string?)

(s/def ::new-project (s/keys :req-un [::name]))

(s/def ::project (s/keys :req-un [::name]))
