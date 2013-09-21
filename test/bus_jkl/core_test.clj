(ns bus-jkl.core-test
  (:use clojure.test
        bus-jkl.core
        midje.sweet)
  (:require [clojure.set :as set]))

;; Test utils

(defn- has-count [expected]
  (fn [coll] (= (count coll) expected)))

(defn- contains-kv-pairs? [found expected]
  (= (select-keys found (keys expected)) expected))

(defn contains-keys? [target-map expected-keys]
  (empty? (set/difference (set expected-keys) (set (keys target-map)))))

(defn- contains-kv-pairs-in-each? [found-maps expected-maps]
  (let [matches
        (map (fn [found expected]
               {:match (contains-kv-pairs? found expected)
                :found found
                :expected expected})
             found-maps expected-maps)
        matches-expected (fn [{:keys [match found expected]}]
                           (if match
                             true
                             (do (println "found map: " found)
                                 (println "did not match expected map: " expected)
                                 (println ""))))]
    (every? matches-expected matches)))

(defn- contains-maps-having [& expected-maps]
  (fn [found-maps]
    (let [outcome (contains-kv-pairs-in-each? found-maps expected-maps)
          found-expected-number-of-maps (= (count found-maps) (count expected-maps))]
      (if found-expected-number-of-maps
        outcome
        (do (println "Number of found maps did not match expected number of maps! \n"
                     "Found: " (count found-maps)
                     "Expected: " (count expected-maps)))))))

(defn- contains-maps-with-keys? [& expected-keys]
  (fn [found-maps]
    (let [matches
          (map (fn [found-map]
                 {:match (contains-keys? found-map expected-keys)
                  :found found-map
                  :expected expected-keys})
               found-maps)
          matches-expected (fn [{:keys [match found expected]}]
                             (if match
                               true
                               (do (println "map with keys: " (keys found))
                                   (println "did not have expected keys: " expected-keys)
                                   (println ""))))]
      (every? matches-expected matches))))



;;-----------------------------------------------
;; buses-for
;;-----------------------------------------------

(def buses-for (ns-resolve 'bus-jkl.core 'buses-for))

(def found-line-data-one-line
  [{:number 27
    :title ["Kauppatori" "Mustalampi"]
    :valid ["03.06.2013" "08.08.2013"]
    :districts ["HEIKKILÄ" "KAUPPATORI" "KELTINMÄKI" "MUSTALAMPI"]
    :route [{:stop "Väinönkatu" :info "pysäkki 10"}
            {:stop "Yliopistonkatu"}
            {:stop "Keltinmäki"}]
    :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
                :buses
                [{:time "05:35"}
                 {:time "06:35"}
                 {:time "07:05"}{:time "07:35"}
                 {:time "08:05"}{:time "08:35"}
                 {:time "09:05"}{:time "09:35"}]}]}])

(fact "A result bus has number, time, title, valid, districts and route"
      (buses-for {:weekday "ma"
                  :numbers [27]
                  :bus-count 1
                  :time "04:00"}
                 found-line-data-one-line)
      => (contains-maps-with-keys? :number :time :title :valid :districts :route))

(fact "Returns one bus when when bus count 1 and only one line applies"
      (buses-for {:weekday "ti"
                  :time "06:00"
                  :bus-count 1} found-line-data-one-line)
      => (has-count 1))

(fact "Returns the next two buses given bus-count of 2"
      (buses-for {:weekday "ti"
                  :time "06:00"
                  :bus-count 2} found-line-data-one-line)
      => (contains-maps-having
          {:number 27 :time "06:35"}
          {:number 27 :time "07:05"}))

(fact "Request for line that has no buses for requested day returns empty vector."
      (buses-for {:weekday "la"
                  :time "07:00"}
                 found-line-data-one-line)
      => [])

(def found-line-data-many-lines
  [{:number 27
    :title ["Kauppatori" "Mustalampi"]
    :valid ["03.06.2013" "08.08.2013"]
    :districts ["HEIKKILÄ" "KAUPPATORI" "KELTINMÄKI" "MUSTALAMPI"]
    :route [{:stop "Foo street"}]
    :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
                :buses [{:time "08:00"}{:time "10:00"}]}]}
   {:number 12
    :title ["Keskusta" "Keltinmäki"]
    :valid ["03.06.2013" "08.08.2013"]
    :districts ["KAUPPATORI" "KELTINMÄKI"]
    :route [{:stop "Bar street"}]
    :schedule [{:day ["pe" "la" "su"]
                :buses [{:time "08:15"}{:time "12:00"}]}]}
   {:number 99
    :title ["Oz" "Keskusta"]
    :valid ["03.06.2013" "08.08.2013"]
    :districts ["HOLLYWOOD" "KESKUSTA"]
    :route [{:stop "Hill street"}]
    :schedule [{:day ["ma" "ti" "ke"]
                :buses [{:time "09:00"}{:time "11:00"}]}
               {:day ["la" "su"]
                :buses [{:time "08:00"}{:time "10:00"}{:time "10:30"}]}]}])

(fact (str "Returns all buses from time onwards for the correct schedule "
           "when when 1 line explicitly defined in request "
           "and weekday not in the first schedule")
      (buses-for {:weekday "la"
                  :numbers [99]
                  :time "08:30"}
                 found-line-data-many-lines)
      => (contains-maps-having
          {:number 99 :time "10:00"}
          {:number 99 :time "10:30"}))

(fact (str "Returns all buses from time onwards for the correct schedule "
           "when when 1 line explicitly defined in request "
           "and weekday not in the first schedule")
      (buses-for {:weekday "la"
                  :numbers [99]
                  :time "08:30"}
                 found-line-data-many-lines)
      => (contains-maps-having
          {:number 99 :time "10:00"}
          {:number 99 :time "10:30"}))

(fact (str "Returns all buses from time onwards from two separate lines"
           "when lines (numbers) not limited in request"
           "the closest buses are on different line")
      (buses-for {:weekday "la"
                  :time "07:00"}
                 found-line-data-many-lines)
      => (contains-maps-having
          {:number 99 :time "08:00"}
          {:number 12 :time "08:15"}
          {:number 99 :time "10:00"}
          {:number 99 :time "10:30"}
          {:number 12 :time "12:00"}))


(fact "Request with only irrelevant key-vals returns empty vector"
      (buses-for {:foo "lorem"
                  :bar "ipsum"}
                 found-line-data-many-lines)
      => [])

;;-----------------------------------------------
;; invalid-request?
;;-----------------------------------------------

(def invalid-request? (ns-resolve 'bus-jkl.core 'invalid-request?))

(fact (str "invalid-request? returns true given empty request map")
      (invalid-request? {}) => true)

(fact (str "invalid-request? returns true given request map with irrelevant keys")
      (invalid-request? {:foo "a" :bar "b"}) => true)

;;-----------------------------------------------
;; times-from-line
;;-----------------------------------------------

(def times-from-line (ns-resolve 'bus-jkl.core 'times-from-line))

(let [line-data [{:time "05:35"}
                 {:time "06:35"}
                 {:time "07:05"}{:time "07:35"}
                 {:time "08:05"}{:time "08:35"}
                 {:time "09:05"}{:time "09:35"}]])

(fact "Returns all remaining buses when time given but no bus-count"
      (times-from-line {:time "06:00"}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}])
      => [{:time "06:35"}
          {:time "07:05"}])

(fact "Returns all buses when time before the first in schedule and no bus-count"
      (times-from-line {:time "05:00"}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}])
      => [{:time "05:35"}
          {:time "06:35"}
          {:time "07:05"}])

(fact "Returns the first bus when time before the first in schedule and bus-count 1"
      (times-from-line {:time "05:00" :bus-count 1}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}])
      => [{:time "05:35"}])

(fact "Returns all buses when time before the first in schedule and bus-count larger than available buses"
      (times-from-line {:time "05:00" :bus-count 9}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}])
      => [{:time "05:35"}
          {:time "06:35"}
          {:time "07:05"}])

(fact "Returns the remaining buses limited by the bus-count"
      (times-from-line {:time "06:00" :bus-count 2}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}
                        {:time "08:05"}])
      => [{:time "06:35"}
          {:time "07:05"}])

(fact "Returns no buses when no buses before time and within minute limit"
      (times-from-line {:time "06:00" :within 15}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}
                        {:time "08:05"}])
      => [])

(fact "Returns one bus when no buses before time and only one bus within the limit"
      (times-from-line {:time "06:00" :within 40}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}
                        {:time "08:05"}])
      => [{:time "06:35"}])

(fact "Returns two buses when no buses before time and two buses within the limit"
      (times-from-line {:time "06:00" :within 70}
                       [{:time "05:35"}
                        {:time "06:35"}
                        {:time "07:05"}
                        {:time "08:05"}])
      => [{:time "06:35"}
          {:time "07:05"}])


;;-----------------------------------------------
;; mins-from-midnight
;;-----------------------------------------------

(def mins-from-midnight (ns-resolve 'bus-jkl.core 'mins-from-midnight))

(fact "Returns nil for non-number"
      (mins-from-midnight "foo") => nil?)

(fact "Returns zero when time is midnight"
      (mins-from-midnight "00:00") => 0)
(defn- weekday-for [date]
  nil)

(fact "Returns 10 mins for 00:10"
      (mins-from-midnight "00:10") => 10)

(fact "Returns 60 mins for 01:00"
      (mins-from-midnight "01:00") => 60)

(fact "Returns 75 mins for 01:15"
      (mins-from-midnight "01:15") => 75)

(fact "Returns 60 * 24 + 5 = 1445 mins for 24:05"
      (mins-from-midnight "24:05") => 1445)

;;-----------------------------------------------
;; lines-for
;;-----------------------------------------------

(def lines-for (ns-resolve 'bus-jkl.core 'lines-for))

(def line-data [{:number 1
            :title ["Keskusta" "Oz"]
            :districts ["FOO" "BAR"]
            :route [{:stop "foo street"}]}
           {:number 2
            :title ["Oz" "Keskusta"]
            :districts ["FOO" "BAR"]
            :route [{:stop "foo street"}]}
           {:number 3
            :title ["Keskusta" "Keljo"]
            :districts ["FOO" "BAR"]
            :route [{:stop "foo street"}]}])

;; When :from-centre not given

(fact "Empty request returns all lines"
      (lines-for {} line-data)
      => (contains-maps-having {:number 1}{:number 2}{:number 3}))

(fact "Finds 1 line when only one number given"
      (lines-for {:numbers [3]} line-data)
      => (contains-maps-having {:number 3}))

(fact "Finds all lines when no limitations given"
      (lines-for {} line-data)
      => (contains-maps-having {:number 1} {:number 2} {:number 3}))

;; When :from-centre given

(fact "Finds 0 lines from city centre for unknown numbers"
  (lines-for {:numbers [123]
               :from-centre true
               :destination "irrelevant"}
              found-line-data-many-lines)
  => [])

(fact "Finds 1 line from city centre when number given but no destination"
  (lines-for {:numbers [3]
               :from-centre true}
              line-data)
  => (contains-maps-having {:number 3}))

(fact "Finds all lines from city centre when number no number or destination given"
      (lines-for {:from-centre true} line-data)
      => (contains-maps-having {:number 1} {:number 3}))

(fact "Finds a single line from city centre when title is written in varying case"
  (lines-for {:from-centre true}
              [{:number 1
                :title ["kesKUSTA" "Oz"]
                :districts ["FOO" "BAR"]
                :route [{:stop "foo street"}]}])
  => (contains-maps-having {:number 1}))

(fact "Interpretes kauppatori as city centre as well"
  (lines-for {:from-centre true}
              [{:number 1
                :title ["Kauppatori" "Oz"]
                :districts ["FOO" "BAR"]
                :route [{:stop "foo street"}]}])
  => (contains-maps-having {:number 1}))

(fact "Finds all lines NOT from city centre when number no number or destination given"
  (lines-for {:from-centre false} line-data)
  => (contains-maps-having {:number 2}))

;; Tests for choosing lines by :destination

(def dest-check-line-data
  [{:number 1
    :title ["Keskusta" "viitaniemi" "Oz"]
    :districts ["harju" "oz"]
    :route [{:stop "a st"}{:stop "b st"}{:stop "dest st"}]}
   {:number 2
    :title ["Oz" "Keltinmäki"]
    :districts ["oz" "harju" "viitaniemi" "valhalla"]
    :route [{:stop "dest st"}{:stop "e st"}{:stop "f st"}]}
   {:number 3
    :title ["Keskusta" "Keltinmäki" "Mustalampi"]
    :districts ["valhalla" "harju"]
    :route [{:stop "x st"}{:stop "dest st"}{:stop "viitaniemi"}]}])

;; Checking destination from TITLE

(fact "Finds lines having destination in the TITLE but not as the first item"
      (lines-for {:destination "Keltinmäki"} dest-check-line-data)
      => (contains-maps-having {:number 2} {:number 3}))

;; Checking destination from DISTRICTS

(fact "Finds lines having destination in the DISTRICTS but not as the first one"
      (lines-for {:destination "harju"} dest-check-line-data)
      => (contains-maps-having {:number 2} {:number 3}))

;; Checking destination from the ROUTE (= the street names)

(fact "Finds lines having destination in the ROUTE but not as the first one"
      (lines-for {:destination "dest st"}
                         dest-check-line-data)
      => (contains-maps-having {:number 1} {:number 3}))

;; Checking destination combining TITLE and DISTRICT and ROUTE

(fact "Finds lines having destination in either TTILE, DISTRICT or ROUTE but not as the first item."
      (lines-for {:destination "viitaniemi"}
                         dest-check-line-data)
      => (contains-maps-having {:number 1} {:number 2} {:number 3}))

(def day-check-line-data
  [{:number 1
    :title ["Keskusta" "viitaniemi" "Oz"]
    :districts ["harju" "oz"]
    :route [{:stop "a st"}{:stop "b st"}{:stop "dest st"}]
    :schedule [{:day ["ma" "ti" "ke" "to"]
                :buses [{:time "08:30"}{:time "08:45"}]}]}
   {:number 2
    :title ["Oz" "Keltinmäki"]
    :districts ["oz" "harju" "viitaniemi" "valhalla"]
    :route [{:stop "dest st"}{:stop "e st"}{:stop "f st"}]
    :schedule [{:day ["la" "su"]
                :buses [{:time "08:45"}{:time "09:00"}]}]}
   {:number 3
    :title ["Keskusta" "Keltinmäki" "Mustalampi"]
    :districts ["valhalla" "harju"]
    :route [{:stop "x st"}{:stop "dest st"}{:stop "viitaniemi"}]
    :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
                :buses [{:time "09:00"}{:time "11:00"}]}
               {:day ["la" "su"]
                :buses [{:time "09:00"}{:time "11:00"}]}]}])


(fact "Returns the only line that is valid for the day"
      (lines-for {:weekday "pe"} day-check-line-data)
      => (contains-maps-having
          {:number 3}))

(fact "Returns the two lines that are valid for the day"
      (lines-for {:weekday "ma"} day-check-line-data)
      => (contains-maps-having
          {:number 1}{:number 3}))

(fact (str "Returns the valid lines when some line has several schedules"
           "and weekday found not found in first schedule")
      (lines-for {:weekday "la"} day-check-line-data)
      => (contains-maps-having
          {:number 2}{:number 3}))

;; (def data
;;   [{:number 27
;;     :title ["Kauppatori" "Mustalampi"]
;;     :valid ["03.06.2013" "08.08.2013"]
;;     :districts ["HEIKKILÄ" "KAUPPATORI" "KELTINMÄKI" "MUSTALAMPI"]
;;     :route [{:stop "Foo street"}]
;;     :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
;;                 :buses [{:time "08:00"}{:time "10:00"}]}]}
;;    {:number 12
;;     :title ["Keskusta" "Keltinmäki"]
;;     :valid ["03.06.2013" "08.08.2013"]
;;     :districts ["KAUPPATORI" "KELTINMÄKI"]
;;     :route [{:stop "Bar street"}]
;;     :schedule [{:day ["pe" "la" "su"]
;;                 :buses [{:time "08:15"}{:time "12:00"}]}]}
;;    {:number 99
;;     :title ["Oz" "Keskusta"]
;;     :valid ["03.06.2013" "08.08.2013"]
;;     :districts ["HOLLYWOOD" "KESKUSTA"]
;;     :route [{:stop "Hill street"}]
;;     :schedule [{:day ["ma" "ti" "ke"]
;;                 :buses [{:time "09:00"}{:time "11:00"}]}
;;                {:day ["la" "su"]
;;                 :buses [{:time "08:00"}{:time "10:00"}{:time "10:30"}]}]}])

;; (def data-
;;   [{:number 27
;;     :title ["Kauppatori" "Mustalampi"]
;;     :valid ["03.06.2013" "08.08.2013"]
;;     :districts ["HEIKKILÄ" "KAUPPATORI" "KELTINMÄKI" "MUSTALAMPI"]
;;     :route [{:stop "Mustalammentie"}]
;;     :schedule [{:day ["ma" "ti" "ke" "to" "pe"]
;;                 :buses [{:time "08:05"}]}
;;                {:day ["la"]
;;                 :buses [{:time "08:35" :info ["ajetaan Keltinmäkeen"]}]}
;;                {:day ["su"]
;;                 :buses [{:time "23:05"}]}]}])

;; (fact (str "Returns the valid lines when some line has several schedules"
;;            "and weekday found not found in first schedule")
;;   (buses-for {} data-)
;;   => [])
