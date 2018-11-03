(ns tales.events.view-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [tales.events.core]
            [tales.subs.view]
            [tales.helper :refer [with-mounted-component]]))

(deftest test-events-view
  (testing "viewport set size"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [size (rf/subscribe [:viewport/original-size])]
        (rf/dispatch [:viewport/set-size [123 321]])
        (is (= @size [123 321])))))
  (testing "camera moving"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [position (rf/subscribe [:camera/position])]
        (rf/dispatch [:camera/move-to [10 20]])
        (is (= @position [10 20]))
        (rf/dispatch [:camera/move-to [20 10]])
        (is (= @position [20 10])))))
  (testing "camera zooming"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [position (rf/subscribe [:camera/position])
            scale (rf/subscribe [:camera/scale])]
        (rf/dispatch [:camera/set-scale 1])
        (is (= @scale 1))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-out])
        (is (= @scale 2))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-out])
        (is (= @scale 4))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-in])
        (is (= @scale 2))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-in])
        (is (= @scale 1))
        (is (= @position [0 0])))))
  (testing "camera zooming around"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [position (rf/subscribe [:camera/position])
            scale (rf/subscribe [:camera/scale])]
        (rf/dispatch [:camera/set-scale 1])
        (is (= @scale 1))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-in [0 0]])
        (is (= @scale 0.5))
        (is (= @position [0 0]))
        (rf/dispatch [:camera/zoom-out [10 20]])
        (is (= @scale 1))
        (is (= @position [-10 -20]))
        (rf/dispatch [:camera/zoom-in [100 200]])
        (is (= @scale 0.5))
        (is (= @position [45 90]))
        (rf/dispatch [:camera/zoom-out [90 0]])
        (is (= @scale 1))
        (is (= @position [0 180])))))
  (testing "camera fit rect"
    (rf-test/run-test-sync
      (rf/dispatch-sync [:initialise-db])
      (rf/dispatch [:viewport/set-size [100 100]])
      (rf/dispatch [:camera/set-aspect-ratio [1 1]])
      (rf/dispatch [:project/add {:slug "my-tale"
                                  :dimensions {:width 100 :height 100}}])
      (let [position (rf/subscribe [:camera/position])
            scale (rf/subscribe [:camera/scale])]
        (rf/dispatch [:camera/fit-rect {:x 0 :y 0 :width 100 :height 100}])
        (is (= @scale 100))
        (is (= @position [50 50]))
        (rf/dispatch [:camera/fit-rect {:x 50 :y 50 :width 50 :height 50}])
        (is (= @scale 50))
        (is (= @position [75 75]))
        (rf/dispatch [:camera/fit-rect {:x 20 :y 20 :width 25 :height 25}])
        (is (= @scale 25))
        (is (= @position [32.5 32.5]))))))
