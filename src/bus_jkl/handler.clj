(ns bus-jkl.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [bus-jkl.core :as bus]))

(defroutes app-routes
  (GET "/" [] {:body (bus/buses {:from-centre true
                                 :bus-count 2
                                 :time "08:00"
                                 :weekday "ma"
                                 :numbers [27]})})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))
