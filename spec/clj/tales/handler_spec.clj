(ns tales.handler-spec
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [ring.mock.request :as mock]
            [ring.util.io :refer [string-input-stream]]
            [ring.util.response :refer [get-header]]
            [speclj.core :refer :all]
            [tales.project :refer [*project-dir*]]
            [tales.handler :refer [app]]
            [tales.project :as project]))

(defn tmp-projects [f]
  (binding [*project-dir* (fs/temp-dir "tales-")]
    (f)
    (fs/delete-dir *project-dir*)))

(describe "web endpoint"
          (it "GET / returns html"
              (let [response (app (-> (mock/request :get "/")))]
                (should= 200 (:status response))
                (should= "text/html; charset=utf-8" (get-header response "Content-Type")))))

(describe "api endpoint"
          (around [f] (tmp-projects f))

          (describe "get tales"
                    (it "sets json content-type"
                        (let [response (app (-> (mock/request :get "/api/tales")))]
                          (should= 200 (:status response))
                          (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

                    (it "returns empty list for empty projects"
                        (let [response (app (-> (mock/request :get "/api/tales")))
                              body (json/read-str (:body response) :key-fn keyword)]
                          (should= 200 (:status response))
                          (should (empty? body))))

                    (it "returns existing projects"
                        (let [project1 (project/create "Project 1")
                              project2 (project/create "Project 2")
                              project3 (project/create "Project 3")
                              response (app (-> (mock/request :get "/api/tales")))
                              body (json/read-str (:body response) :key-fn keyword)]
                          (should= 200 (:status response))
                          (should= 3 (count body))
                          (should= project1 (nth body 0))
                          (should= project2 (nth body 1))
                          (should= project3 (nth body 2)))))

          (describe "create tale"
                    (it "fails for invalid params"
                        (let [response (app (-> (mock/request :post "/api/tales")
                                                (mock/json-body {})))
                              body (json/read-str (:body response) :key-fn keyword)]
                          (should= 400 (:status response))
                          (should= "application/json; charset=utf-8" (get-header response "Content-Type"))
                          (should-not-be-nil (:error body))))

                    (it "sets json content-type"
                        (let [response (app (-> (mock/request :post "/api/tales")
                                                (mock/json-body {:name "My Tale"})))
                              content-type (get-header response "Content-Type")]
                          (should= 201 (:status response))
                          (should= "application/json; charset=utf-8" content-type)))

                    (it "creates resource"
                        (let [response (app (-> (mock/request :post "/api/tales")
                                                (mock/json-body {:name "My Tale"})))
                              location (get-header response "Location")]
                          (should= 201 (:status response))
                          (should (str/ends-with? location "/api/tales/my-tale"))))

                    (it "returns the created project"
                        (let [response (app (-> (mock/request :post "/api/tales")
                                                (mock/json-body {:name "My Tale"})))
                              body (json/read-str (:body response) :key-fn keyword)]
                          (should= 201 (:status response))
                          (should= "My Tale" (:name body))
                          (should= "my-tale" (:slug body)))))

          (describe "get tale"
                    (it "sets json content-type"
                        (let [_ (project/create "Test")
                              response (app (-> (mock/request :get "/api/tales/test")))]
                          (should= 200 (:status response))
                          (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

                    (it "returns not-found for non-existing project"
                        (let [response (app (-> (mock/request :get "/api/tales/test")))]
                          (should= 404 (:status response))))

                    (it "returns project"
                        (let [project (project/create "Project 1")
                              response (app (-> (mock/request :get "/api/tales/project-1")))
                              body (json/read-str (:body response) :key-fn keyword)]
                          (should= 200 (:status response))
                          (should= project body))))

          (it "PUT /api/tales/tale returns json"
              (let [response (app (-> (mock/request :put "/api/tales/tale")))]
                (should= 200 (:status response))
                (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

          (it "DELETE /api/tales/tale returns json"
              (let [response (app (-> (mock/request :delete "/api/tales/tale")))]
                (should= 200 (:status response))
                (should= "application/json; charset=utf-8" (get-header response "Content-Type")))))

(describe "image upload"
          (it "copies uploaded file to project directory"
              (let [form-body (str/join "\r\n" ["--XXXX"
                                                "Content-Disposition: form-data;name=\"file\"; filename=\"test.txt\""
                                                "Content-Type: text/plain"
                                                ""
                                                "foo"
                                                "--XXXX--"])
                    response (app (-> (mock/request :put "/api/tales/test/image")
                                      (mock/content-type "multipart/form-data; boundary=XXXX")
                                      (mock/content-length (count form-body))
                                      (mock/body form-body)))
                    target-file (fs/file *project-dir* "test" "test.txt")]
                (should= 200 (:status response))
                (should= "application/octet-stream" (get-header response "Content-Type"))
                (should (fs/exists? target-file)))))
