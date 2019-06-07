(ns tales.views-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as reagent :refer [atom]]
            [tales.views.editor :as editor]
            [tales.views.project :as project]
            [tales.core]
            [tales.helper :refer [with-mounted-component]]))

(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))

(deftest test-project-page
  (testing "contains heading in tell page"
    (with-mounted-component (project/page)
      (fn [_ div]
        (is (found-in #"Enter the name of your tale and press enter" div))))))

(deftest test-editor-page
  (testing "contains heading in editor page"
    (with-mounted-component (editor/page)
      (fn [_ div]
        (is (found-in #"You haven't uploaded a poster yet." div))))))
