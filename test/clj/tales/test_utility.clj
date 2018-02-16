(ns tales.test-utility
  (:require [ring.util.response :refer [get-header]]
            [me.raynes.fs :as fs]))

(defn content-type? [content-type response]
  (= content-type (get-header response "Content-Type")))

(defn content-type-json? [response]
  (content-type? "application/json; charset=utf-8" response))

(defn temporary-projects [f]
  (binding [tales.project/*project-dir* (fs/temp-dir "tales-")]
    (f)
    (fs/delete-dir tales.project/*project-dir*)))
