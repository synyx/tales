(ns tales.subs.editor
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :editor
  (fn [db _]
    (:editor db)))