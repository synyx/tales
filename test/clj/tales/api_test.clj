(ns tales.api_test
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [ring.mock.request :as mock]
            [ring.util.io :refer [string-input-stream]]
            [ring.util.response :refer [get-header]]
            [tales.api :as api]
            [tales.handler :refer [app]]
            [tales.test-utility :refer [content-type?
                                        content-type-json?
                                        temporary-projects]]))

(use-fixtures :each temporary-projects)

(deftest test-api-get-projects
  (testing "returns empty list for empty projects"
    (let [response (app (mock/request :get "/api/tales"))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (empty? body))))

  (testing "returns existing projects"
    (let [project1 (:body (api/create-project {:name "Project 1"}))
          project2 (:body (api/create-project {:name "Project 2"}))
          project3 (:body (api/create-project {:name "Project 3"}))
          response (app (mock/request :get "/api/tales"))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= 3 (count body)))
      (is (some #{project1} body))
      (is (some #{project2} body))
      (is (some #{project3} body)))))

(deftest test-api-get-project
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :get "/api/tales/unknown"))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "returns project"
    (let [project (:body (api/create-project {:name "Test"}))
          response (app (mock/request :get "/api/tales/test"))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= project body)))))

(deftest test-api-create-project
  (testing "fails for invalid params"
    (let [response (app (-> (mock/request :post "/api/tales")
                          (mock/json-body {})))
          body (json/read-str (:body response) :key-fn keyword)]
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
    (let [_ (api/create-project {:name "Existing"})
          response (app (-> (mock/request :post "/api/tales")
                          (mock/json-body {:name "Existing"})))
          location (get-header response "Location")]
      (is (= 409 (:status response)))
      (is (content-type-json? response))
      (is (not location))))

  (testing "returns the created project"
    (let [response (app (-> (mock/request :post "/api/tales")
                          (mock/json-body {:name "My Tale"})))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 201 (:status response)))
      (is (content-type-json? response))
      (is (= "my-tale" (:slug body)))
      (is (= "My Tale" (:name body))))))

(deftest test-api-update-project
  (testing "returns not-found for non-existing project"
    (let [response (app (-> (mock/request :put "/api/tales/unknown")
                          (mock/json-body {:name "My Tale"})))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "fails for invalid params"
    (let [_ (api/create-project {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test")
                          (mock/json-body {})))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (content-type-json? response))
      (is (not (nil? (:error body))))))

  (testing "returns the updated project"
    (let [_ (api/create-project {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test")
                          (mock/json-body {:name "My Tale"})))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= "test" (:slug body)))
      (is (= "My Tale" (:name body))))))

(deftest test-api-delete-project
  (testing "returns not-found for non-existing project"
    (let [response (app (mock/request :delete "/api/tales/unknown"))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "deletes resource"
    (let [_ (api/create-project {:name "Test"})
          first-try (app (mock/request :delete "/api/tales/test"))
          second-try (app (mock/request :delete "/api/tales/test"))]
      (is (= 200 (:status first-try)))
      (is (content-type-json? first-try))
      (is (= 404 (:status second-try)))
      (is (content-type-json? second-try)))))

(deftest test-image-type
  (testing "recognizes gif images"
    (is (= "gif" (api/image-type "image/gif"))))
  (testing "recognizes gif images"
    (is (= "png" (api/image-type "image/png"))))
  (testing "recognizes png images"
    (is (= "jpg" (api/image-type "image/jpeg"))))
  (testing "recognizes bmp images"
    (is (= "bmp" (api/image-type "image/bmp"))))
  (testing "recognizes svg images"
    (is (= "svg" (api/image-type "image/svg+xml"))))
  (testing "fails for unknown image types"
    (is (not (api/image-type nil)))
    (is (not (api/image-type "image/foo")))))

(def example-file (str/join "\n" ["<svg version=\"1.1\""
                                  "  baseProfile=\"full\""
                                  "  width=\"300\" height=\"200\""
                                  "  xmlns=\"http://www.w3.org/2000/svg\">"
                                  "    <circle cx=\"150\" cy=\"100\" r=\"80\""
                                  "      fill=\"green\" />"
                                  "</svg>"]))

(deftest test-api-update-project-image
  (testing "returns not-found for non-existing project"
    (let [response (app (-> (mock/request :put "/api/tales/unknown/image")
                          (mock/content-type "image/svg+xml")
                          (mock/content-length (count example-file))
                          (mock/body example-file)))]
      (is (= 404 (:status response)))
      (is (content-type-json? response))))

  (testing "returns bad-request for unsupported content-type"
    (let [_ (api/create-project {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test/image")
                          (mock/content-type "text/plain")
                          (mock/content-length (count example-file))
                          (mock/body example-file)))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 400 (:status response)))
      (is (content-type-json? response))
      (is (= "Invalid content-type: text/plain" (:error body)))))

  (testing "copies uploaded file to project directory"
    (let [_ (api/create-project {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test/image")
                          (mock/content-type "image/svg+xml")
                          (mock/content-length (count example-file))
                          (mock/body example-file)))
          target-file (fs/file tales.project/*project-dir* "test" "test.svg")]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (fs/exists? target-file))))

  (testing "returns the updated project"
    (let [_ (api/create-project {:name "Test"})
          response (app (-> (mock/request :put "/api/tales/test/image")
                          (mock/content-type "image/svg+xml")
                          (mock/content-length (count example-file))
                          (mock/body example-file)))
          body (json/read-str (:body response) :key-fn keyword)]
      (is (= 200 (:status response)))
      (is (content-type-json? response))
      (is (= "test" (:slug body)))
      (is (= "test.svg" (:file-path body))))))
