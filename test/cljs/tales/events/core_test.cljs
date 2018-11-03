(ns tales.events.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [tales.events.core]
            [tales.subs.core]))

(deftest test-events-core
  (testing "activate page"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [active-page (rf/subscribe [:active-page])]
        (rf/dispatch [:activate-page :test-page])
        (is (= @active-page :test-page))))))