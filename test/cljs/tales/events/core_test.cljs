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
        (is (= @active-project example-project))))))