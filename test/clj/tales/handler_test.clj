(ns tales.handler_test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [tales.handler :refer [app]]
            [tales.test-utility :refer [content-type?]]))

(deftest test-web-endpoint
  (testing "returns html"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response)))
      (is (content-type? "text/html; charset=utf-8" response)))))
