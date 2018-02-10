(ns tales.api
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [ring.util.response :refer [created response]]
            [tales.project :as project :refer [*project-dir*]]
            [clojure.string :as str])
  (:import (java.io File)))

(defn bad-request
  [body]
  {:status  400
   :headers {}
   :body    body})

(defn find-all []
  (response (project/find-all)))

(defn find-by-slug [slug]
  (response {:action "find-by-slug"
             :slug   slug}))

(defn create [body]
  (if (s/valid? :tales.project/new-project body)
    (let [project (project/create (:name body))
          resource (format "/api/tales/%s" (:slug project))]
      (created resource project))
    (bad-request {:error (s/explain-str :tales.project/new-project body)})))

(defn update [slug body]
  (response {:action "update"
             :slug   slug
             :body   body}))

(defn delete [slug]
  (response {:action "delete"
             :slug   slug}))

(defn upload-image [slug file]
  (let [file-name (file :filename)
        file-size (file :size)
        temp-file (file :tempfile)
        target-file (io/as-file (str/join (File/separator) [*project-dir* slug file-name]))]
    (do
      (io/make-parents target-file)
      (io/copy temp-file target-file)
      {:status 200})))
