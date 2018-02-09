(ns tales.api
  (:require [clojure.java.io :as io]
            [config.core :refer [env]]
            [ring.util.response :refer [response]]))

(defn project-dir []
  (or
    (env :project-dir)
    (format "%s/Tales" (System/getProperty "user.home"))))

(defn find-all []
  (response {:action "find-all"}))

(defn find-by-slug [slug]
  (response {:action "find-by-slug"
             :slug   slug}))

(defn create [body]
  (response {:action "create"
             :body   body}))

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
        target-file (io/as-file (format "%s/%s/%s" (project-dir) slug file-name))]
    (do
      (io/make-parents target-file)
      (io/copy temp-file target-file)
      {:status 200})))
