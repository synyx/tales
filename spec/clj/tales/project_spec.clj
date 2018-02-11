(ns tales.project-spec
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.test :as test]
            [me.raynes.fs :as fs]
            [speclj.core :refer :all]
            [tales.project :as project :refer [*project-dir*]]))

(defn tmp-projects [f]
  (binding [*project-dir* (fs/temp-dir "tales-")]
    (f)
    (fs/delete-dir *project-dir*)))

(describe "get projects"
          (around [f] (tmp-projects f))

          (it "returns empty list for empty projects"
              (let [projects (project/find-all)]
                (should (empty? projects))))

          (it "returns list of projects"
              (let [project1 (project/create "Project 1")
                    project2 (project/create "Project 2")
                    project3 (project/create "Project 3")
                    projects (project/find-all)]
                (should= 3 (count projects))
                (should= project1 (nth projects 0))
                (should= project2 (nth projects 1))
                (should= project3 (nth projects 2)))))

(describe "get project"
          (around [f] (tmp-projects f))

          (it "returns nil for non-existing project"
              (let [project (project/find-by-slug "test")]
                (should-be-nil project)))

          (it "returns project"
              (let [project (project/create "Test")
                    found-project (project/find-by-slug "test")]
                (should= project found-project))))

(describe "create project"
          (around [f] (tmp-projects f))

          (it "returns the created project"
              (let [project (project/create "Test")]
                (should= "test" (:slug project))
                (should= "Test" (:name project))))

          (it "creates project file on disk"
              (let [project (project/create "Test")
                    project-file (fs/file *project-dir* (:slug project) "config.edn")]
                (should (fs/exists? project-file))))

          (it "content of project file conforms to spec"
              (let [project (project/create "Test")
                    project-file (fs/file *project-dir* (:slug project) "config.edn")
                    loaded-project (edn/read-string (slurp project-file))]
                (should (s/valid? :tales.project/project loaded-project))
                (should= (dissoc project :slug) loaded-project))))
