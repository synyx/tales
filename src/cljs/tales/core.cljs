(ns tales.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame :refer [dispatch dispatch-sync subscribe]]
            [tales.events]
            [tales.routes :as routes]
            [tales.subs]
            [tales.views.editor :refer [editor-page]]
            [tales.views.project :refer [project-page]]))

(defn main-page []
  (let [project (subscribe [:active-project])]
    (if-not (nil? @project)
      [editor-page]
      [project-page])))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [main-page] (.getElementById js/document "app"))
  (.addEventListener js/window "resize" #(dispatch [:window-resize (-> % .-target)])))

(defn init! []
  (dispatch-sync [:initialise-db])
  (routes/init!)
  (mount-root))
