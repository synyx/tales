(ns tales.project-spec
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.test :as test]
            [speclj.core :refer :all]
            [tales.project :as project :refer [*project-dir*]])
  (:import (java.io File)))

(defn cleanup-projects [f]
  (let [tmp-dir (System/getProperty "java.io.tmpdir")
        project-dir (str/join (File/separator) [tmp-dir "tales"])]
    (binding [*project-dir* project-dir]
      (f)
      (tales.utility/delete-recursively *project-dir*))))

(describe "get projects"
          (around [f] (cleanup-projects f))

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

(describe "create project"
          (around [f] (cleanup-projects f))

          (it "returns the created project"
              (let [project (project/create "Test")]
                (should= "test" (:slug project))
                (should= "Test" (:name project))))

          (it "creates project file on disk"
              (let [project (project/create "Test")
                    project-file (str/join (File/separator) [*project-dir* (:slug project) "config.edn"])]
                (should (.exists (io/as-file project-file)))))

          (it "content of project file conforms to spec"
              (let [project (project/create "Test")
                    project-file (str/join (File/separator) [*project-dir* (:slug project) "config.edn"])
                    loaded-project (edn/read-string (slurp project-file))]
                (should (s/valid? :tales.project/project loaded-project))
                (should= (dissoc project :slug) loaded-project))))
