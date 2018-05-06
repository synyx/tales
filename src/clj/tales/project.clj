(ns tales.project
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [config.core :refer [env]]
            [me.raynes.fs :as fs]
            [tales.utility :refer [slugify]]))

(def ^:dynamic *project-dir* (or
                               (fs/file (env :project-dir))
                               (fs/file (fs/home) "Tales")))

(s/def ::name string?)
(s/def ::file-path string?)

(s/def ::width (s/and int? pos?))
(s/def ::height (s/and int? pos?))
(s/def ::dimensions (s/keys :req-un [::width ::height]))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::coordinate (s/keys ::req-un [::x ::y]))
(s/def ::bottom-left ::coordinate)
(s/def ::top-right ::coordinate)

(s/def ::rect (s/keys ::req-un [::bottom-left ::top-right]))
(s/def ::slide (s/keys ::req-un [::rect]))
(s/def ::slides (s/coll-of ::slide))

(s/def ::project (s/keys :req-un [::name]
                   :opt-un [::file-path ::dimensions ::slides]))

(defn- config-file [slug]
  (fs/file *project-dir* slug "config.edn"))

(defn project? [slug]
  (and (not-empty slug)
    (fs/exists? (config-file slug))))

(defn project-names []
  (reverse
    (filter project?
      (mapv fs/name
        (filter fs/directory? (fs/list-dir *project-dir*))))))

(defn load-project [slug]
  (if (project? slug)
    (let [project (edn/read-string (slurp (config-file slug)))]
      (assoc project :slug slug))))

(defn load-projects []
  (mapv load-project (project-names)))

(defn save-project [slug project]
  (if (and (seq slug)
        (s/valid? ::project project))
    (let [loaded-project (load-project slug)
          project (merge loaded-project project)
          filename (config-file slug)]
      (fs/mkdirs (fs/parent filename))
      (spit filename (pr-str (dissoc project :slug)))
      (assoc project :slug slug))))

(defn delete-project [slug]
  (if (project? slug)
    (fs/delete-dir (fs/file *project-dir* slug))
    false))
