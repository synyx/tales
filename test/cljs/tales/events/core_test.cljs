(ns tales.events.core-test
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

  (testing "activate page"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [active-page (rf/subscribe [:active-page])]
        (rf/dispatch [:activate-page :test-page])
        (is (= @active-page :test-page)))))

  (testing "activate project"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale"}
            active-project (rf/subscribe [:active-project])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (is (= @active-project example-project)))))

  (testing "activate slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale"}
            active-slide (rf/subscribe [:active-slide])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (rf/dispatch [:activate-slide 1])
        (is (= @active-slide 1)))))

  (testing "move between slides"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides [{} {} {}]}
            active-slide (rf/subscribe [:active-slide])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (rf/dispatch [:activate-slide 1])
        (is (= @active-slide 1))
        (rf/dispatch [:next-slide])
        (is (= @active-slide 2))
        (rf/dispatch [:next-slide])
        (is (= @active-slide 0))
        (rf/dispatch [:prev-slide])
        (is (= @active-slide 2))
        (rf/dispatch [:prev-slide])
        (is (= @active-slide 1)))))

  (testing "move change slide order"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides [{:id 0} {:id 1} {:id 2}]}
            active-slide (rf/subscribe [:active-slide])
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:activate-project "my-tale"])
        (rf/dispatch [:activate-slide 1])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:change-order 1])
        (is (= @active-slide 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2))
        (is (= (:id (nth @slides 2)) 1))
        (rf/dispatch [:change-order -1])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:change-order 2])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:change-order -2])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))))))