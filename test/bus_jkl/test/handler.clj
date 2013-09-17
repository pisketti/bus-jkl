(ns bus-jkl.test.handler
  (:use clojure.test
        ring.mock.request
        bus-jkl.handler))

;;UNCOMMENT WHEN WORKING AGAIN

;; (deftest test-app
;;   (testing "main route"
;;     (let [response (app (request :get "/"))]
;;       (is (= (:status response) 200))

;;       ;;replace with something relevant
;;       ;;(is (= (:body response) "Hello World"))
;;       ))

;;   (testing "not-found route"
;;     (let [response (app (request :get "/invalid"))]
;;       (is (= (:status response) 404)))))
