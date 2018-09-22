(ns tales.api
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [ring.util.response :refer [created response not-found get-header]]
            [tales.project :as project :refer [*project-dir*]]))

(defn bad-request [body] {:status 400 :headers {} :body body})
(defn conflict [body] {:status 409 :headers {} :body body})

(defn get-projects []
  (response (project/load-projects)))

(defn get-project [slug]
  (let [project (project/load-project slug)]
    (if project
      (response project)
      (not-found {}))))

(defn create-project [project]
  (if (s/valid? ::project/project project)
    (let [slug (tales.utility/slugify (:name project))
          resource (format "/api/tales/%s" slug)]
      (if-not (project/project? slug)
        (created resource (project/save-project slug project))
        (conflict {})))
    (bad-request {:error (s/explain-str ::project/project project)})))

(defn update-project [slug project]
  (if (project/project? slug)
    (if (s/valid? ::project/project project)
      (response (project/save-project slug project))
      (bad-request {:error (s/explain-str ::project/project project)}))
    (not-found {})))

(defn delete-project [slug]
  (if (project/project? slug)
    (do (project/delete-project slug)
        (response {}))
    (not-found {})))

(defn image-type [content-type]
  (case content-type
    "image/gif" "gif"
    "image/png" "png"
    "image/jpeg" "jpg"
    "image/bmp" "bmp"
    "image/svg+xml" "svg"
    false))

(defn update-project-image [slug request]
  (let [content-type (get-header request "Content-Type")
        extension (image-type content-type)
        file-name (str/join "." [slug extension])
        target-file (fs/file *project-dir* slug file-name)]
    (if (project/project? slug)
      (if extension
        (with-open [out (io/output-stream target-file)]
          (io/copy (:body request) out)
          (response
            (project/save-project slug (assoc
                                        (project/load-project slug)
                                         :file-path file-name))))
        (bad-request {:error (str "Invalid content-type: " content-type)}))
      (not-found {}))))
