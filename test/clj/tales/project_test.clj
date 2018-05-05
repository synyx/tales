(ns tales.project_test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [tales.project :as project]
            [tales.test-utility :refer [temporary-projects]]))

(use-fixtures :each temporary-projects)

(deftest test-load-projects
  (testing "returns empty list for empty projects"
    (let [projects (project/load-projects)]
      (is (empty? projects))))

  (testing "returns list of projects"
    (let [project1 (project/save-project "project-1" {:name "Project 1"})
          project2 (project/save-project "project-2" {:name "Project 2"})
          project3 (project/save-project "project-3" {:name "Project 3"})
          projects (project/load-projects)]
      (is (= 3 (count projects)))
      (is (some #{project1} projects))
      (is (some #{project2} projects))
      (is (some #{project3} projects)))))

(deftest test-load-project
  (testing "returns nil for nil"
    (is (nil? (project/load-project nil))))

  (testing "returns nil for non-existing project"
    (is (nil? (project/load-project "test"))))

  (testing "returns project"
    (let [project (project/save-project "test" {:name "Test"})
          found-project (project/load-project "test")]
      (is (= project found-project))))

  (testing "returned project conforms to spec"
    (let [_ (project/save-project "test" {:name "Test"})
          found-project (project/load-project "test")]
      (is (s/valid? :tales.project/project found-project)))))

(deftest test-save-project
  (testing "returns nil for empty slug"
    (is (not (project/save-project nil {:name "Test"}))))

  (testing "returns nil for non-conforming project"
    (is (not (project/save-project "test" nil))))

  (testing "returns the saved project"
    (let [project (project/save-project "test" {:name "Test"})]
      (is (= "test" (:slug project)))
      (is (= "Test" (:name project)))))

  (testing "returns the updated project"
    (let [project (project/save-project "test" {:name "Test"})
          updated-project (project/save-project "test"
                            (assoc project :name "Updated Test"))]
      (is (= "test" (:slug updated-project)))
      (is (= "Updated Test" (:name updated-project))))))

(deftest test-delete-project
  (testing "returns false for nil"
    (is (not (project/delete-project nil))))

  (testing "returns false for non-existing project"
    (is (not (project/delete-project "xyz"))))

  (testing "returns true for existing project"
    (let [_ (project/save-project "test" {:name "Test"})]
      (is (project/delete-project "test")))))
