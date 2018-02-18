(ns tales.api_test
  (:require [clojure.data.json :as json]
            [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [ring.mock.request :as mock]
            [ring.util.io :refer [string-input-stream]]
            [ring.util.response :refer [get-header]]
            [tales.api :as api]
            [tales.handler :refer [app]]
            [tales.test-utility :refer [content-type? content-type-json? temporary-projects]]))

(use-fixtures :each temporary-projects)

(deftest test-api-get-tales
  (testing "returns empty list for empty projects"
    (let [response (app (mock/request :get "/api/tales"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (empty? body))))

  (testing "returns existing projects"
    (let [project1 (:body (api/create {:name "Project 1"}))
          project2 (:body (api/create {:name "Project 2"}))
          project3 (:body (api/create {:name "Project 3"}))
          response (app (mock/request :get "/api/tales"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= 3 (count body)))
      (is (= project1 (nth body 0)))
      (is (= project2 (nth body 1)))
      (is (= project3 (nth body 2))))))

(deftest test-api-get-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :get "/api/tales/unknown"))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "returns project"
    (let [project  (:body (api/create {:name "Test"}))
          response (app (mock/request :get "/api/tales/test"))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= project body)))))

(deftest test-api-create-tale
  (testing "fails for invalid params"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (content-type-json? response))
      (is (not (nil? (:error body))))))

  (testing "creates resource"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {:name "Test"})))
          location (get-header response "Location")]
      (is (= 201 (:status response)))
      (is (content-type-json? response))
      (is (clojure.string/ends-with? location "/api/tales/test"))))

  (testing "only creates resource once"
    (let [_        (api/create {:name "Existing"})
          response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {:name "Existing"})))
          location (get-header response "Location")]
      (is (= 409 (:status response)))
      (is (content-type-json? response))
      (is (not location))))

  (testing "returns the created project"
    (let [response (app (-> (mock/request :post "/api/tales")
                            (mock/json-body {:name "My Tale"})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 201 (:status response)))
      (is (content-type-json? response))
      (is (= "my-tale" (:slug body)))
      (is (= "My Tale" (:name body))))))

(deftest test-api-update-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (-> (mock/request :put "/api/tales/unknown")
                            (mock/json-body {:name "My Tale"})))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "fails for invalid params"
    (let [_        (api/create {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test")
                            (mock/json-body {})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (content-type-json? response))
      (is (not (nil? (:error body))))))

  (testing "returns the updated project"
    (let [_        (api/create {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test")
                            (mock/json-body {:name "My Tale"})))
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= "test" (:slug body)))
      (is (= "My Tale" (:name body))))))

(deftest test-api-delete-tale
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :delete "/api/tales/unknown"))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "deletes resource"
    (let [_          (api/create {:name "Test"})
          first-try  (app (mock/request :delete "/api/tales/test"))
          second-try (app (mock/request :delete "/api/tales/test"))]
      (is (= 200 (:status first-try)))
      (is (content-type-json? first-try))
      (is (= 404 (:status second-try)))
      (is (content-type-json? second-try)))))

(def test-form (clojure.string/join "\r\n" ["--XXXX"
                                            "Content-Disposition: form-data;name=\"file\"; filename=\"test.txt\""
                                            "Content-Type: text/plain"
                                            ""
                                            "foo"
                                            "--XXXX--"]))

(deftest test-api-image-upload
  (testing "returns not-found for non-existing project"
    (let [form-body test-form
          response  (app (-> (mock/request :put "/api/tales/unknown/image")
                             (mock/content-type "multipart/form-data; boundary=XXXX")
                             (mock/content-length (count form-body))
                             (mock/body form-body)))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "copies uploaded file to project directory"
    (let [_           (api/create {:name "Test"})
          form-body   test-form
          response    (app (-> (mock/request :put "/api/tales/test/image")
                               (mock/content-type "multipart/form-data; boundary=XXXX")
                               (mock/content-length (count form-body))
                               (mock/body form-body)))
          target-file (fs/file tales.project/*project-dir* "test" "test.txt")]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (fs/exists? target-file))))

  (testing "returns the updated project"
    (let [_         (api/create {:name "Test"})
          form-body test-form
          response  (app (-> (mock/request :put "/api/tales/test/image")
                             (mock/content-type "multipart/form-data; boundary=XXXX")
                             (mock/content-length (count form-body))
                             (mock/body form-body)))
          body      (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= "test" (:slug body)))
      (is (= "test.txt" (:file-path body))))))
