(ns bus-jkl.handler
  (:use compojure.core
        bus-jkl.core)
  (:require [bus-jkl.request :as req]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.walk :as walk]
            [clojure.data.json :as json]))



(defn- query-buses [http-req-params]
  (-> http-req-params
      req/line-numbers-to-seq
      req/bus-count-to-int
      req/return-fields-to-seq
      buses))

(defn- client []
  (resp/resource-response "index.html" {:root "public"}))

(defroutes app-routes
  (GET "/json" {params :params} (-> params
                                    query-buses
                                    resp/response))
  (GET "/client" [] (client))
  (GET "/" [] (client))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))
