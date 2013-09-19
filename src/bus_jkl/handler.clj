(ns bus-jkl.handler
  (:use compojure.core
        [bus-jkl.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [clojure.walk :as walk]))

;; (defroutes app-routes
;;   (GET "/" [] {:body (bus/buses {:from-centre true
;;                                  :bus-count 2
;;                                  :time "08:00"
;;                                  :weekday "ma"
;;                                  :numbers [27]})})
;;   (route/resources "/")
;;   (route/not-found "Not Found"))

(defn query-buses [httpreq-params]
  (let [bus-request (walk/keywordize-keys httpreq-params)]
    ;;(assoc bus-request :new "val")
    (buses bus-request)
    ))

(defroutes app-routes
  (GET "/" {params :params} {:body (query-buses params)})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))
