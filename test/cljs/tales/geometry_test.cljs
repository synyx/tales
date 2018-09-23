(ns tales.geometry-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [tales.geometry :as geometry]))

(deftest test-geometry
  (testing "zoom to scale conversion"
    (is (= (geometry/zoom->scale 0) 1))
    (is (= (geometry/zoom->scale 2) 4))
    (is (= (geometry/zoom->scale -2) 0.25)))
  (testing "scale to zoom conversion"
    (is (= (geometry/scale->zoom 1) 0))
    (is (= (geometry/scale->zoom 4) 2))
    (is (= (geometry/scale->zoom 0.25) -2)))
  (testing "scale point"
    (is (= (geometry/scale {:x 0 :y 0} 1) {:x 0 :y 0}))
    (is (= (geometry/scale {:x 10 :y 20} 5) {:x 2 :y 4}))
    (is (= (geometry/scale {:x 10 :y 20} 0.5) {:x 20 :y 40})))
  (testing "unscale point"
    (is (= (geometry/unscale {:x 0 :y 0} 1) {:x 0 :y 0}))
    (is (= (geometry/unscale {:x 10 :y 20} 5) {:x 50 :y 100}))
    (is (= (geometry/unscale {:x 10 :y 20} 0.5) {:x 5 :y 10})))
  (testing "add two points"
    (is (= (geometry/add-points {:x 10 :y 20} {:x 10 :y 20}) {:x 20 :y 40}))
    (is (= (geometry/add-points {:x 10 :y 20} {:x 70 :y 90}) {:x 80 :y 110})))
  (testing "distance between points"
    (is (= (geometry/distance {:x 10 :y 20} {:x 10 :y 20}) {:x 0 :y 0}))
    (is (= (geometry/distance {:x 10 :y 20} {:x 70 :y 90}) {:x 60 :y 70})))
  (testing "center of rect"
    (is (= (geometry/rect-center {:x 10 :y 20 :width 30 :height 40})
          {:x 25 :y 40})))
  (testing "move point"
    (is (= (geometry/move-point {:x 10 :y 20} 30 40) {:x 40 :y 60})))
  (testing "move rect"
    (is (= (geometry/move-rect {:x 10 :y 20 :width 10 :height 20} 30 40)
          {:x 40 :y 60 :width 10 :height 20})))
  (testing "normalize rect"
    (is (= (geometry/normalize-rect {:x 10 :y 20 :width 30 :height 40})
          {:x 10 :y 20 :width 30 :height 40}))
    (is (= (geometry/normalize-rect {:x -10 :y -20 :width 30 :height 40})
          {:x -10 :y -20 :width 30 :height 40}))
    (is (= (geometry/normalize-rect {:x 10 :y 20 :width -30 :height -40})
          {:x -20 :y -20 :width 30 :height 40}))
    (is (= (geometry/normalize-rect {:x -10 :y -20 :width -30 :height -40})
          {:x -40 :y -60 :width 30 :height 40})))
  (testing "resize rect"
    (let [rect {:x 10 :y 20 :width 30 :height 40}]
      (is (= (geometry/resize-rect rect :top-left 10 20)
            {:x 20 :y 40 :width 20 :height 20}))
      (is (= (geometry/resize-rect rect :top-right 10 20)
            {:x 10 :y 40 :width 40 :height 20}))
      (is (= (geometry/resize-rect rect :bottom-right 10 20)
            {:x 10 :y 20 :width 40 :height 60}))
      (is (= (geometry/resize-rect rect :bottom-left 10 20)
            {:x 20 :y 20 :width 20 :height 60}))
      (is (= (geometry/resize-rect rect :bottom-right -40 -50)
            {:x 0 :y 10 :width 10 :height 10}))))
  (testing "rect scale"
    (let [rect {:x 10 :y 20 :width 30 :height 40}]
      (is (= (geometry/rect-scale rect rect) 1))
      (is (= (geometry/rect-scale rect {:x 10 :y 20 :width 15 :height 10}) 2))
      (is (= (geometry/rect-scale rect {:x 10 :y 20 :width 30 :height 80}) 0.5)))))