(ns tales.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as reagent :refer [atom]]
            [tales.core :as rc]))


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


(deftest test-editor-page
  (testing "contains heading in editor page"
    (with-mounted-component (rc/editor-page)
                            (fn [c div]
                              (is (found-in #"Welcome to tales" div))))))

(deftest test-tell-page
  (testing "contains heading in tell page"
    (with-mounted-component (rc/tell-page)
                            (fn [c div]
                              (is (found-in #"Now tell your tale..." div))))))
