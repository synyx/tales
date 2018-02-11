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
    (let [project (project/create "Test")
          found-project (project/find-by-slug "test")]
      (is (= project found-project)))))

(deftest test-create-project
  (testing "returns the created project"
    (let [project (project/create "Test")]
      (is (= "test" (:slug project)))
      (is (= "Test" (:name project)))))

  (testing "saves project on disk"
    (let [project (project/create "Test")
          project-file (fs/file *project-dir* (:slug project) "config.edn")]
      (is (fs/exists? project-file))))

  (testing "content of project file conforms to spec"
    (let [project (project/create "Test")
          project-file (fs/file *project-dir* (:slug project) "config.edn")
          loaded-project (edn/read-string (slurp project-file))]
      (is (s/valid? :tales.project/project loaded-project))
      (is (= (dissoc project :slug) loaded-project)))))
