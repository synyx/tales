(ns tales.events.stage-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [tales.events.core]
            [tales.subs.stage]
            [tales.helper :refer [with-mounted-component]]))

(deftest test-events-stage
  (testing "stage mounting"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (with-mounted-component [:div]
        (fn [_ div]
          (let [dom-node (rf/subscribe [:stage/dom-node])]
            (rf/dispatch [:stage/mounted div])
            (is (= @dom-node div))
            (rf/dispatch [:stage/unmounted])
            (is (nil? @dom-node)))))))
  (testing "stage moving"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [position (rf/subscribe [:stage/position])
            origin (rf/subscribe [:stage/transform-origin])]
        (rf/dispatch [:stage/move-to 10 20])
        (is (= @position {:x 10 :y 20}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/move-to 20 10])
        (is (= @position {:x 20 :y 10}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/move-by 10 -10])
        (is (= @position {:x 30 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/move-by -40 -10])
        (is (= @position {:x -10 :y -10}))
        (is (= @origin {:x 0 :y 0})))))
  (testing "stage zooming"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [zoom (rf/subscribe [:stage/zoom])
            position (rf/subscribe [:stage/position])
            origin (rf/subscribe [:stage/transform-origin])]
        (rf/dispatch [:stage/zoom 1])
        (is (= @zoom 1))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/zoom-out])
        (is (= @zoom 0))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/zoom-out 2])
        (is (= @zoom -2))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/zoom-in 2])
        (is (= @zoom 0))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/zoom-in])
        (is (= @zoom 1))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0})))))
  (testing "stage zooming around"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [zoom (rf/subscribe [:stage/zoom])
            position (rf/subscribe [:stage/position])
            origin (rf/subscribe [:stage/transform-origin])]
        (rf/dispatch [:stage/zoom-around 1 {:x 0 :y 0}])
        (is (= @zoom 1))
        (is (= @position {:x 0 :y 0}))
        (is (= @origin {:x 0 :y 0}))
        (rf/dispatch [:stage/zoom-out-around {:x 10 :y 20}])
        (is (= @zoom 0))
        (is (= @position {:x -10 :y -20}))
        (is (= @origin {:x 10 :y 20}))
        (rf/dispatch [:stage/zoom-in-around {:x 100 :y 200}])
        (is (= @zoom 1))
        (is (= @position {:x 45 :y 90}))
        (is (= @origin {:x 100 :y 200}))
        (rf/dispatch [:stage/zoom-out-around {:x 90 :y 0}])
        (is (= @zoom 0))
        (is (= @position {:x 0 :y 180}))
        (is (= @origin {:x 90 :y 0})))))
  (testing "stage fit rect"
    (rf-test/run-test-sync
      (rf/dispatch-sync [:initialise-db])
      (with-mounted-component [:div {:style {:width "100px" :height "100px"}}]
        (fn [_ div]
          (rf/dispatch [:stage/mounted div])
          (let [zoom (rf/subscribe [:stage/zoom])
                position (rf/subscribe [:stage/position])
                origin (rf/subscribe [:stage/transform-origin])]
            (rf/dispatch [:stage/fit-rect {:x 0 :y 0 :width 100 :height 100}])
            (is (= @zoom 0))
            (is (= @position {:x 50 :y 50}))
            (is (= @origin {:x 50 :y 50}))
            (rf/dispatch [:stage/fit-rect {:x 50 :y 50 :width 50 :height 50}])
            (is (= @zoom 1))
            (is (= @position {:x 75 :y 75}))
            (is (= @origin {:x 75 :y 75}))
            (rf/dispatch [:stage/fit-rect {:x 20 :y 20 :width 25 :height 25}])
            (is (= @zoom 2))
            (is (= @position {:x 32.5 :y 32.5}))
            (is (= @origin {:x 32.5 :y 32.5}))))))))