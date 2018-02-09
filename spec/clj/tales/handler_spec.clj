(ns tales.handler-spec
  (:require [clojure.java.io :as io]
            [speclj.core :refer :all]
            [ring.mock.request :as mock]
            [ring.util.io :refer [string-input-stream]]
            [ring.util.response :refer [get-header]]
            [tales.api :refer [project-dir]]
            [tales.handler :refer [app]]
            [clojure.string :as str]))

(describe "web endpoint"
          (it "GET / returns html"
              (let [response (app (-> (mock/request :get "/")))]
                (should= 200 (:status response))
                (should= "text/html; charset=utf-8" (get-header response "Content-Type")))))

(describe "api endpoint"
          (it "GET /api/tales returns json"
              (let [response (app (-> (mock/request :get "/api/tales")))]
                (should= 200 (:status response))
                (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

          (it "POST /api/tales returns json"
              (let [response (app (-> (mock/request :post "/api/tales")))]
                (should= 200 (:status response))
                (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

          (it "GET /api/tales/tale returns json"
              (let [response (app (-> (mock/request :get "/api/tales/tale")))]
                (should= 200 (:status response))
                (should= "application/json; charset=utf-8" (get-header response "Content-Type"))))

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
                    target-file (format "%s/%s/%s" (project-dir) "test" "test.txt")]
                (should= 200 (:status response))
                (should= "application/octet-stream" (get-header response "Content-Type"))
                (should (.exists (io/as-file target-file))))))
