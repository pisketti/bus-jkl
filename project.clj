(defproject bus-jkl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [clj-time "0.6.0"]
                 [midje "1.5.1"]
                 [org.clojure/tools.trace "0.7.6"]
                 [org.clojure/data.json "0.2.3"]]
  :plugins [[lein-ring "0.8.5"]
            [lein-midje "3.0.1"]]
  :ring {:handler bus-jkl.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
