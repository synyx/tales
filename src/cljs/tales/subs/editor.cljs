(ns tales.subs.editor
  (:require [re-frame.core :refer [dispatch reg-sub reg-sub-raw]]))

(reg-sub :editor
  (fn [db _]
    (:editor db)))

(reg-sub :editor/current-slide
  :<- [:editor]
  (fn [editor _]
    (get editor :current-slide)))