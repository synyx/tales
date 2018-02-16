(ns tales.handler_test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [ring.mock.request :as mock]
            [ring.util.io :refer [string-input-stream]]
            [ring.util.response :refer [get-header]]
            [tales.handler :refer [app]]
            [tales.project :as project :refer [*project-dir*]]))

(defn tmp-projects [f]
  (binding [*project-dir* (fs/temp-dir "tales-")]
    (f)
    (fs/delete-dir *project-dir*)))

(use-fixtures :each tmp-projects)

(deftest test-web-endpoint
  (testing "returns html"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response)))
      (is (= "text/html; charset=utf-8"
             (get-header response "Content-Type"))))))

(deftest test-api-get-tales
  (testing "returns json"
    (let [response (app (mock/request :get "/api/tales"))]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8"
             (get-header response "Content-Type")))))

  (testing "returns empty list for empty projects"
    (let [response (app (mock/request :get "/api/tales"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (empty? body))))

  (testing "returns existing projects"
    (let [project1 (project/create "Project 1")
          project2 (project/create "Project 2")
          project3 (project/create "Project 3")
          response (app (mock/request :get "/api/tales"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (= 3 (count body)))
      (is (= project1 (nth body 0)))
      (is (= project2 (nth body 1)))
      (is (= project3 (nth body 2))))))

(deftest test-api-get-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :get "/api/tales/test"))]
      (is (= 404 (:status response)))))

  (testing "returns json"
    (let [_        (project/create "Test")
          response (app (mock/request :get "/api/tales/test"))]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8"
             (get-header response "Content-Type")))))

  (testing "returns project"
    (let [project  (project/create "Project 1")
          response (app (mock/request :get "/api/tales/project-1"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (= project body)))))

(deftest test-api-create-tale
  (testing "returns json"
    (let [response     (app (-> (mock/request :post "/api/tales")
                                (mock/json-body {:name "My Tale"})))
          content-type (get-header response "Content-Type")]
      (is (= 201 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "fails for invalid params"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (= "application/json; charset=utf-8"
             (get-header response "Content-Type")))
      (is (not (nil? (:error body))))))

  (testing "creates resource"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {:name "My Tale"})))
          location (get-header response "Location")]
      (is (= 201 (:status response)))
      (is (clojure.string/ends-with? location "/api/tales/my-tale"))))

  (testing "returns the created project"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {:name "My Tale"})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 201 (:status response)))
      (is (= "My Tale" (:name body)))
      (is (= "my-tale" (:slug body))))))

(deftest test-api-update-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (-> (mock/request :put "/api/tales/test")
                            (mock/json-body {:name "My Tale"})))]
      (is (= 404 (:status response)))))

  (testing "returns json"
    (let [_            (project/create "Test")
          response     (app (-> (mock/request :put "/api/tales/test")
                                (mock/json-body {:name "My Tale"})))
          content-type (get-header response "Content-Type")]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8" content-type))))

  (testing "fails for invalid params"
    (let [_        (project/create "Test")
          response (app (-> (mock/request :put "/api/tales/test")
                            (mock/json-body {})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (= "application/json; charset=utf-8"
             (get-header response "Content-Type")))
      (is (not (nil? (:error body))))))

  (testing "returns the updated project"
    (let [_        (project/create "Test")
          response (app (-> (mock/request :put "/api/tales/test")
                            (mock/json-body {:name "My Tale"})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (= "My Tale" (:name body)))
      (is (= "test" (:slug body))))))

(deftest test-api-delete-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :delete "/api/tales/test"))]
      (is (= 404 (:status response)))))

  (testing "returns json"
    (let [_        (project/create "Test")
          response (app (mock/request :delete "/api/tales/test"))]
      (is (= 200 (:status response)))
      (is (= "application/json; charset=utf-8"
             (get-header response "Content-Type")))))

  (testing "deletes resource"
    (let [project  (project/create "Test")
          response (app (mock/request :delete "/api/tales/test"))]
      (is (= 200 (:status response)))
      (is (not (project/project? (:slug project)))))))

(deftest test-api-image-upload
  (testing "copies uploaded file to project directory"
    (let [form-body   (clojure.string/join "\r\n" ["--XXXX"
                                                   "Content-Disposition: form-data;name=\"file\"; filename=\"test.txt\""
                                                   "Content-Type: text/plain"
                                                   ""
                                                   "foo"
                                                   "--XXXX--"])
          response    (app (-> (mock/request :put "/api/tales/test/image")
                               (mock/content-type "multipart/form-data; boundary=XXXX")
                               (mock/content-length (count form-body))
                               (mock/body form-body)))
          target-file (fs/file *project-dir* "test" "test.txt")]
      (is (= 200 (:status response)))
      (is (= "application/octet-stream"
             (get-header response "Content-Type")))
      (is (fs/exists? target-file)))))
