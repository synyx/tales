(ns tales.api
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [ring.util.response :refer [created response not-found get-header]]
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
    (let [slug (tales.utility/slugify (:name body))
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

(defn upload-image [slug request]
  (let [content-type (get-header request "Content-Type")
        extension (case content-type
                    "image/gif" "gif"
                    "image/png" "png"
                    "image/jpeg" "jpg"
                    "image/bmp" "bmp"
                    "image/svg+xml" "svg"
                    false)
        file-name (str/join "." [slug extension])
        target-file (fs/file *project-dir* slug file-name)]
    (if (project/project? slug)
      (if extension
        (with-open [out (io/output-stream target-file)]
          (io/copy (:body request) out)
          (response
            (project/save-project! slug (assoc
                                          (project/load-project! slug)
                                          :file-path file-name))))
        (bad-request {:error (str "Invalid content-type: " content-type)}))
      (not-found {}))))
