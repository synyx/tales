(ns tales.events.slide-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [tales.events.core]
            [tales.subs.core]
            [tales.helper :refer [with-mounted-component]]))

(deftest test-events-slide
  (rf/reg-event-db :project/add
    (fn [db [_ project]]
      (assoc db :project project)))

  (rf/reg-event-db :project/update
    (fn [db [_ project]]
      (assoc db :project project)))

  (testing "add slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides []}
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (is (= (count @slides) 0))
        (rf/dispatch [:slide/add {:id 0}])
        (is (= (count @slides) 1))
        (is (= (:id (nth @slides 0)) 0))
        (rf/dispatch [:slide/add {:id 1}])
        (is (= (count @slides) 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (rf/dispatch [:slide/add {:id 2}])
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
        (is (= (count @slides) 0))
        (rf/dispatch [:slide/add {:id 0}])
        (rf/dispatch [:slide/add {:id 1}])
        (rf/dispatch [:slide/add {:id 2}])
        (rf/dispatch [:slide/activate 1])
        (rf/dispatch [:slide/update {:id 1 :changed true}])
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
        (is (= (count @slides) 0))
        (rf/dispatch [:slide/add {:id 0}])
        (rf/dispatch [:slide/add {:id 1}])
        (rf/dispatch [:slide/add {:id 2}])
        (is (= (count @slides) 3))
        (rf/dispatch [:slide/delete 1])
        (is (= (count @slides) 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2)))))

  (testing "activate slide"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale"}
            active-slide (rf/subscribe [:slide/active])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:slide/activate 1])
        (is (= @active-slide 1)))))

  (testing "move between slides"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides [{} {} {}]}
            active-slide (rf/subscribe [:slide/active])]
        (rf/dispatch [:project/add example-project])
        (rf/dispatch [:slide/activate nil])
        (rf/dispatch [:slide/next])
        (is (= @active-slide 0))
        (rf/dispatch [:slide/activate nil])
        (rf/dispatch [:slide/prev])
        (is (= @active-slide 2))
        (rf/dispatch [:slide/activate 1])
        (is (= @active-slide 1))
        (rf/dispatch [:slide/next])
        (is (= @active-slide 2))
        (rf/dispatch [:slide/next])
        (is (= @active-slide 0))
        (rf/dispatch [:slide/prev])
        (is (= @active-slide 2))
        (rf/dispatch [:slide/prev])
        (is (= @active-slide 1)))))

  (testing "swap slides"
    (rf-test/run-test-sync
      (rf/dispatch [:initialise-db])
      (let [example-project {:slug "my-tale" :slides [{:id 0} {:id 1} {:id 2}]}
            active-slide (rf/subscribe [:slide/active])
            slides (rf/subscribe [:slides])]
        (rf/dispatch [:project/add example-project])
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:slide/swap 1 2])
        (is (= @active-slide 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2))
        (is (= (:id (nth @slides 2)) 1))
        (rf/dispatch [:slide/swap 2 1])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:slide/swap 1 3])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:slide/swap 3 1])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:slide/swap-next 1])
        (is (= @active-slide 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2))
        (is (= (:id (nth @slides 2)) 1))
        (rf/dispatch [:slide/swap-prev 2])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))
        (rf/dispatch [:slide/swap-next])
        (is (= @active-slide 2))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 2))
        (is (= (:id (nth @slides 2)) 1))
        (rf/dispatch [:slide/swap-prev])
        (is (= @active-slide 1))
        (is (= (:id (nth @slides 0)) 0))
        (is (= (:id (nth @slides 1)) 1))
        (is (= (:id (nth @slides 2)) 2))))))