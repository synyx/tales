(ns tales.helper
  (:require [reagent.core :as r :refer [atom]]))

(def is-client (not (nil? (try (.-document js/window)
                               (catch js/Object e nil)))))

(defn- add-test-div [name]
  (let [doc js/document
        body (.-body js/document)
        div (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when is-client
    (let [div (add-test-div "_testreagent")]
      (let [comp (r/render-component comp div #(f comp div))]
        (r/unmount-component-at-node div)
        (r/flush)
        (.removeChild (.-body js/document) div)))))