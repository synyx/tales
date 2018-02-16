(ns tales.project_test
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [tales.project :as project :refer [*project-dir*]]))

(defn tmp-projects [f]
  (binding [*project-dir* (fs/temp-dir "tales-")]
    (f)
    (fs/delete-dir *project-dir*)))

(use-fixtures :each tmp-projects)

(deftest test-get-projects
  (testing "returns empty list for empty projects"
    (let [projects (project/find-all)]
      (is (empty? projects))))

  (testing "returns list of projects"
    (let [project1 (project/create "Project 1")
          project2 (project/create "Project 2")
          project3 (project/create "Project 3")
          projects (project/find-all)]
      (is (= 3 (count projects)))
      (is (= project1 (nth projects 0)))
      (is (= project2 (nth projects 1)))
      (is (= project3 (nth projects 2))))))

(deftest test-get-project
  (testing "returns nil for non-existing project"
    (let [project (project/find-by-slug "test")]
      (is (nil? project))))

  (testing "returns project"
    (let [project       (project/create "Test")
          found-project (project/find-by-slug "test")]
      (is (= project found-project))))

  (testing "returned project conforms to spec"
    (let [_             (project/create "Test")
          found-project (project/find-by-slug "test")]
      (is (s/valid? :tales.project/project found-project)))))

(deftest test-create-project
  (testing "returns the created project"
    (let [project (project/create "Test")]
      (is (= "test" (:slug project)))
      (is (= "Test" (:name project)))))

  (testing "saves project on disk"
    (let [project      (project/create "Test")
          project-file (fs/file *project-dir* (:slug project) "config.edn")]
      (is (fs/exists? project-file)))))

(deftest test-update-project
  (testing "does nothing for non-existing project"
    (let [project (project/update "test" {:name "Test"})]
      (is (nil? project))
      (is (not (project/project? "test")))))

  (testing "returns the updated project"
    (let [project (project/create "Test")
          updated-project (project/update (:slug project) (assoc project :name "Update"))]
      (is (= "test" (:slug updated-project)))
      (is (= "Update" (:name updated-project))))))

(deftest test-delete-project
  (testing "returns false for non-existing project"
    (is (not (project/project? "xyz")))
    (is (not (project/delete "xyz"))))

  (testing "returns true for existing project"
    (let [project (project/create "Test")]
      (is (project/project? (:slug project)))
      (is (project/delete (:slug project))))))
