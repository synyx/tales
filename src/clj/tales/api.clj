(ns tales.api
  (:require [ring.util.response :refer [response]]))

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
