(ns tales.events.project-test
  (:require [cljs.test :refer-macros [is are deftest testing]]
            [tales.events.project :as project]))

(deftest test-events-project-open
  (testing "sets loading state"
    (let [db {}
          event []
          {db' :db} (project/open db event)]
      (is (true? (get-in db' [:loading? :project])))))
  (testing "makes request to remote"
    (let [db {}
          event ["my-tale"]
          {req :http-xhrio} (project/open db event)]
      (is (= (:method req) :get))
      (is (= (:uri req) "/api/tales/my-tale"))
      (is (= (:on-success req) [:project/open-success]))
      (is (= (:on-failure req) [:api-request-error :project])))))

(deftest test-events-project-open-success
  (testing "unsets loading state"
    (let [db {}
          event []
          {db' :db} (project/open-success db event)]
      (is (false? (get-in db' [:loading? :project])))))
  (testing "unsets errors"
    (let [db {:errors [:error1 :error2]}
          event []
          {db' :db} (project/open-success db event)]
      (is (nil? (:errors db')))))
  (testing "resets project from response"
    (let [db {}
          project {:slug "my-tale"}
          event [project]
          {db' :db} (project/open-success db event)]
      (is (= project (:project db'))))))

(deftest test-events-project-get-all
  (testing "sets loading state"
    (let [db {}
          {db' :db} (project/get-all db)]
      (is (true? (get-in db' [:loading? :projects])))))
  (testing "makes request to remote"
    (let [db {}
          {req :http-xhrio} (project/get-all db)]
      (is (= (:method req) :get))
      (is (= (:uri req) "/api/tales/"))
      (is (= (:on-success req) [:project/get-all-success]))
      (is (= (:on-failure req) [:api-request-error :projects])))))

(deftest test-events-project-get-all-success
  (testing "unsets loading state"
    (let [db {}
          event []
          db' (project/get-all-success db event)]
      (is (false? (get-in db' [:loading? :projects])))))
  (testing "unsets errors"
    (let [db {:errors [:error1 :error2]}
          event []
          db' (project/get-all-success db event)]
      (is (nil? (:errors db')))))
  (testing "resets project from response"
    (let [db {}
          project1 {:slug "my-first-tale"}
          project2 {:slug "my-second-tale"}
          event [[project1 project2]]
          db' (project/get-all-success db event)]
      (is (= 2 (count (:projects db'))))
      (is (= project1 (get-in db' [:projects "my-first-tale"])))
      (is (= project2 (get-in db' [:projects "my-second-tale"]))))))

(deftest test-events-project-add
  (testing "sets loading state"
    (let [db {}
          event []
          {db' :db} (project/add db event)]
      (is (true? (get-in db' [:loading? :project])))))
  (testing "makes request to remote"
    (let [db {}
          project {:slug "my-tale"}
          event [project]
          {req :http-xhrio} (project/add db event)]
      (is (= (:method req) :post))
      (is (= (:uri req) "/api/tales/"))
      (is (= (:params req) project))
      (is (= (:on-success req) [:project/change-success]))
      (is (= (:on-failure req) [:api-request-error :project])))))

(deftest test-events-project-update
  (testing "sets loading state"
    (let [db {}
          event []
          {db' :db} (project/change db event)]
      (is (true? (get-in db' [:loading? :project])))))
  (testing "makes request to remote"
    (let [db {}
          project {:slug "my-tale"}
          event [project]
          {req :http-xhrio} (project/change db event)]
      (is (= (:method req) :put))
      (is (= (:uri req) "/api/tales/my-tale"))
      (is (= (:params req) project))
      (is (= (:on-success req) [:project/change-success]))
      (is (= (:on-failure req) [:api-request-error :project])))))

(deftest test-events-project-change-success
  (testing "unsets loading state"
    (let [db {}
          event []
          {db' :db} (project/change-success db event)]
      (is (false? (get-in db' [:loading? :project])))))
  (testing "unsets errors"
    (let [db {:errors [:error1 :error2]}
          event []
          {db' :db} (project/change-success db event)]
      (is (nil? (:errors db')))))
  (testing "resets project from response"
    (let [db {}
          project {:slug "my-tale"}
          event [project]
          {db' :db} (project/change-success db event)]
      (is (= project (get-in db' [:projects "my-tale"])))))
  (testing "navigates to project"
    (let [db {}
          event [{:slug "my-tale"}]
          {navigate :navigate} (project/change-success db event)]
      (is (= "/editor/my-tale/" navigate)))))

(deftest test-events-project-api-request-error
  (testing "sets errors from response"
    (let [db {}
          errors ["unknown error"]
          event [:test-request {:response {:errors errors}}]
          {db' :db} (project/api-request-error db event)]
      (is (= errors (get-in db' [:errors :test-request])))))
  (testing "dispatches complete request event"
    (let [db {}
          event [:test-request nil]
          {event :dispatch} (project/api-request-error db event)]
      (is (= (first event) :complete-request))
      (is (= (second event) :test-request)))))

(deftest test-events-project-complete-request
  (testing "unsets loading state"
    (let [db {:loading? {:test-request true}}
          event [:test-request]
          db' (project/complete-request db event)]
      (is (false? (get-in db' [:loading? :test-request]))))))
