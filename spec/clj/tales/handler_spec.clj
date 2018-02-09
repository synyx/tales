(ns tales.handler-spec
  (:require [speclj.core :refer :all]
            [ring.mock.request :as mock]
            [ring.util.response :refer [get-header]]
            [tales.handler :refer [app]]))

(describe "test app"
          (it "GET / returns html"
              (let [response (app (-> (mock/request :get "/")))]
                (should= 200 (:status response))
                (should= "text/html; charset=utf-8" (get-header response "Content-Type"))))

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
