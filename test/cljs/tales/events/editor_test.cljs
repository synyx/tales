(ns tales.events.editor-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [tales.events.core]
            [tales.subs.core]
            [tales.helper :refer [with-mounted-component]]))

(deftest test-events-stage
  (rf/reg-event-db :project/add
    (fn [db [_ project]]
      (assoc-in db [:projects (:slug project)] project)))

  (rf/reg-event-db :project/update
    (fn [db [_ project]]
      (assoc-in db [:projects (:slug project)] project)))

  (testing "add slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides []}
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (is (= (count @slides) 0))
        (rf/dispatch [:editor/add-slide {:id 0}])
        (is (= (count @slides) 1))
        (is (= (:id (nth @slides 0)) 0))
        (rf/dispatch [:editor/add-slide {:id 1}])
        (is (= (count @slides) 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (rf/dispatch [:editor/add-slide {:id 2}])
        (is (= (count @slides) 3))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2)))))

  (testing "update slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides []}
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (is (= (count @slides) 0))
        (rf/dispatch [:editor/add-slide {:id 0}])
        (rf/dispatch [:editor/add-slide {:id 1}])
        (rf/dispatch [:editor/add-slide {:id 2}])
        (rf/dispatch [:activate-slide 1])
        (rf/dispatch [:editor/update-slide {:id 1 :changed true}])
        (is (= (count @slides) 3))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (is (:changed (nth @slides 1)))
        (is (not (:changed (nth @slides 0))))
        (is (not (:changed (nth @slides 2)))))))

  (testing "delete active slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides []}
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (is (= (count @slides) 0))
        (rf/dispatch [:editor/add-slide {:id 0}])
        (rf/dispatch [:editor/add-slide {:id 1}])
        (rf/dispatch [:editor/add-slide {:id 2}])
        (is (= (count @slides) 3))
        (rf/dispatch [:editor/delete-active-slide])
        (is (= (count @slides) 3))
        (rf/dispatch [:activate-slide 1])
        (rf/dispatch [:editor/delete-active-slide])
        (is (= (count @slides) 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2))))))