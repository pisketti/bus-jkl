(ns bus-jkl.handler
  (:use compojure.core
        [bus-jkl.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.walk :as walk]
            [clojure.data.json :as json]))

(defn query-buses [httpreq-params]
  (let [bus-request (walk/keywordize-keys httpreq-params)]
    (buses bus-request)))

(defn client []
  (resp/resource-response "index.html" {:root "public"}))

(defroutes app-routes
  (GET "/json" {params :params} (-> params
                                    query-buses
                                    resp/response))
  (GET "/client" [] (client))
  (GET "/" [] (client))
  (route/resources "/")
  (route/not-found "Not Found"))

(defn line-numbers-to-vec [handler]
  (fn [request]
    request))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))
