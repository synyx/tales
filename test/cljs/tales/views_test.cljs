(ns tales.views-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as reagent :refer [atom]]
            [tales.views.editor :refer [editor-page]]
            [tales.views.project :refer [project-page]]
            [tales.subs]))


(def isClient (not (nil? (try (.-document js/window)
                              (catch js/Object e nil)))))

(def rflush reagent/flush)

(defn add-test-div [name]
  (let [doc js/document
        body (.-body js/document)
        div (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when isClient
    (let [div (add-test-div "_testreagent")]
      (let [comp (reagent/render-component comp div #(f comp div))]
        (reagent/unmount-component-at-node div)
        (reagent/flush)
        (.removeChild (.-body js/document) div)))))

(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))

(deftest test-project-page
  (testing "contains heading in tell page"
    (with-mounted-component (project-page)
      (fn [c div]
        (.log js/console div)
        (is (found-in #"Enter the name of your tale and press enter" div))))))

(deftest test-editor-page
  (testing "contains heading in editor page"
    (with-mounted-component (editor-page)
      (fn [c div]
        (is (found-in #"You haven't uploaded a poster yet." div))))))
