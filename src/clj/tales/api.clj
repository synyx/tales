(ns tales.api
  (:require [clojure.spec.alpha :as s]
            [me.raynes.fs :as fs]
            [ring.util.response :refer [created response not-found]]
            [tales.project :as project :refer [*project-dir*]]
            [clojure.string :as str]))

(defn bad-request
  [body]
  {:status  400
   :headers {}
   :body    body})

(defn find-all []
  (response (project/find-all)))

(defn find-by-slug [slug]
  (let [project (project/find-by-slug slug)]
    (if project
      (response project)
      (not-found {}))))

(defn create [body]
  (if (s/valid? :tales.project/new-project body)
    (let [project  (project/create (:name body))
          resource (format "/api/tales/%s" (:slug project))]
      (created resource project))
    (bad-request {:error (s/explain-str :tales.project/new-project body)})))

(defn update [slug body]
  (if (s/valid? :tales.project/project body)
    (if (project/project? slug)
        (response (project/update slug body))
        (not-found {}))
    (bad-request {:error (s/explain-str :tales.project/new-project body)})))

(defn delete [slug]
  (if (project/project? slug)
    (do (project/delete slug)
        (response {}))
    (not-found {})))

(defn upload-image [slug file]
  (let [file-name   (file :filename)
        file-size   (file :size)
        temp-file   (file :tempfile)
        target-file (fs/file *project-dir* slug file-name)]
    (fs/copy+ temp-file target-file)
    {:status 200}))
