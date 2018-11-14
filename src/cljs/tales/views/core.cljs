(ns tales.views.core
  (:require [re-frame.core :refer [subscribe]]
            [tales.views.editor :as editor]
            [tales.views.project :as project]
            [tales.views.presenter :as presenter]))

(defn- page [page-name]
  (case page-name
    :home [project/page]
    :editor [editor/page]
    :presenter [presenter/page]
    [project/page]))

(defn main-page []
  (let [active-page (subscribe [:active-page])]
    [page @active-page]))
