(ns tales.api
  (:require [clojure.spec.alpha :as s]
            [me.raynes.fs :as fs]
            [ring.util.response :refer [created response not-found]]
            [tales.project :as project :refer [*project-dir*]]))

(defn bad-request [body] {:status 400 :headers {} :body body})
(defn conflict [body] {:status 409 :headers {} :body body})

(defn find-all []
  (response (project/load-projects!)))

(defn find-by-slug [slug]
  (let [project (project/load-project! slug)]
    (if project
      (response project)
      (not-found {}))))

(defn create [body]
  (if (s/valid? ::project/project body)
    (let [slug     (tales.utility/slugify (:name body))
          resource (format "/api/tales/%s" slug)]
      (if (not (project/project? slug))
        (created resource (project/save-project! slug body))
        (conflict {})))
    (bad-request {:error (s/explain-str ::project/project body)})))

(defn update [slug body]
  (if (project/project? slug)
    (if (s/valid? ::project/project body)
      (response (project/save-project! slug body))
      (bad-request {:error (s/explain-str ::project/project body)}))
    (not-found {})))

(defn delete [slug]
  (if (project/project? slug)
    (do (project/delete-project! slug)
        (response {}))
    (not-found {})))

(defn upload-image [slug file]
  (let [file-name   (file :filename)
        temp-file   (file :tempfile)
        target-file (fs/file *project-dir* slug file-name)]
    (if (project/project? slug)
      (do
        (fs/copy+ temp-file target-file)
        (response
          (project/save-project! slug
                                 (assoc (project/load-project! slug)
                                   :file-path file-name))))
      (not-found {}))))
