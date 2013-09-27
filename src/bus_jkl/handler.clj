(ns bus-jkl.handler
  (:use compojure.core
        [bus-jkl.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :as resp]
            [clojure.walk :as walk]))

(defn query-buses [httpreq-params]
  (let [bus-request (walk/keywordize-keys httpreq-params)]
    (buses bus-request)))

(defn client []
  (resp/resource-response "index.html" {:root "public"}))

(defroutes app-routes
  (GET "/json" {params :params} {:body (query-buses params)})
  (GET "/client" [] (client))
  (GET "/" [] (client))
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))
