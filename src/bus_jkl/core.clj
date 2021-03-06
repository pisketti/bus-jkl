(ns bus-jkl.core
  (:use [clojure.string :only [split upper-case lower-case]]
        [clojure.pprint :only [pprint]]
        [clojure.tools.trace])
  (:require [bus-jkl.data :as data]
            [bus-jkl.util :as util]
            [clojure.set :as set]
            [clj-time.core :as ctc]
            [clj-time.format :as ctf]))

(def ^:dynamic *data* (data/read-data))

(defn- one-of? [str candidates]
  (when str (some (fn [candidate]
                    (= (upper-case str) (when candidate (upper-case candidate))))
                  candidates)))

(defn- filter-by-numbers [numbers lines]
  (filter (fn [line]
            (if numbers
              ((set numbers) (:number line))
              true))
          lines))

;;TODO: can be removed. For debugging only
(defn- prn-ret [msg returnable]
  (do (println msg)
      ;;(doall (pprint returnable))
      (doseq [x returnable]
        (pprint x))
      returnable))

(defn- filter-by-weekday [weekday lines]
  "Filters lines by weekday. Accepts all lines if weekday not given in request."
  (if-not weekday
    lines
    (filter
     (fn [{:keys [schedule]}]
       (let [days-of-line (flatten (map :day schedule))]
         (one-of? weekday days-of-line)))
     lines)))

;;TODO perhaps refactor to make the intent clearer.
;;     Also, consider taking varargs instead of just two fixed args
(defn- has-both? [seq x y]
  "Checks if x and y found in seq in the correct order"
  (->> seq
       (partition-by #(not (= (lower-case x) (lower-case %))))
       rest
       flatten
       (one-of? y)))

(defn matches-path [from destination path]
  (cond (and from destination) (has-both? path from destination)
        from (one-of? from (butlast path))
        destination (one-of? destination (rest path))
        :else false))


(defn- filter-by-from-and-destination [from destination lines]
  "Filters lines by destination. Checks title, districts and route.
   Skips the first in each since destination cannot be where the travelling starts."
  (if (or from destination)
    (filter
     (fn [{:keys [title districts route]}]
       (or (matches-path from destination title)
           (matches-path from destination districts)
           (matches-path from destination (map :stop route))))
     lines)
    lines))

(defn- lines-for [{:keys [numbers from destination weekday] :as request} data]
  (when (and request data)
       (->> data
            (filter-by-numbers numbers)
            (filter-by-from-and-destination from destination)
            (filter-by-weekday weekday))))

(defn- mins-from-midnight [time-str]
  (let [[hours-str mins-str] (split time-str #":")
        mins (util/parse-int mins-str)
        hours (util/parse-int hours-str)]
    (if (and mins hours)
      (+ (* 60 hours) mins))))

(defn- earlier-than [max-time]
  "Returns a function that checks whether a given time is before the max time"
  (let [max-mins (mins-from-midnight max-time)]
    (fn [{:keys [time]}]
      (< (mins-from-midnight time) max-mins))))

(defn- within-time [now within-mins]
  "Returns a function that checks whether a given time is within a time period"
  (let [max-mins (+ (mins-from-midnight now) within-mins)]
    (fn [{:keys [time]}]
      (< (mins-from-midnight time) max-mins))))

(defn- limit-by-count [count buses]
  (if count (take count buses) buses))

(defn- times-from-line [{:keys [time bus-count within]} line-data]
  "Filters the lines that match the request from all lines"
  (let [drop-earlier-buses (fn [time buses] (drop-while (earlier-than time) buses))
        limit-by-time (fn [from-time within buses]
                        (if within (take-while (within-time from-time within) buses) buses))]
    (->> line-data
         (drop-earlier-buses time)
         (limit-by-count bus-count)
         (limit-by-time time within))))

;; Needed only if support for :date in request will be implemented
;; (defn- weekday-for [date]
;;   nil)

(defn- get-valid-buses-for [weekday schedule]
  (some (fn [{:keys [day buses]}] (when (one-of? weekday day) buses))
                 schedule))

(defn- add-metadata-from [matching-line]
  (fn [bus]
    (merge bus
           (select-keys matching-line [:number :title :valid :districts :route]))))

(defn- compare-time [time-str-1 time-str-2]
  (< (mins-from-midnight time-str-1) (mins-from-midnight time-str-2)))

(defn- buses-from-lines [{:keys [weekday] :as request} lines]
  (for [{:keys [schedule] :as line} lines
        :let [buses (get-valid-buses-for weekday schedule)
              matching-buses (times-from-line request buses)]]
    (map (add-metadata-from line) matching-buses)))

(defn- sort-by-time [buses-with-metadata]
  (sort-by :time compare-time buses-with-metadata))

(defn- valid-time? [time]
  (try
    (let [total-mins (mins-from-midnight time)]
      (and (>= total-mins 0) (<= total-mins (* 24 60))))
    (catch Exception e false)))

(defn- valid-weekday? [weekday]
  (one-of? weekday ["ma" "ti" "ke" "to" "pe" "la" "su"]))

(defn- valid-request? [{:keys [time weekday] :as request}]
  (and (valid-time? time)
       (valid-weekday? weekday)))

(defn- buses-for [{:keys [weekday buscount] :as request} lines]
  (if-not (valid-request? request)
    []
    (->> lines
         (lines-for request)
         (buses-from-lines request)
         flatten
         sort-by-time
         (limit-by-count buscount))))

(defn- weekday-from [joda-datetime]
  ({1 "ma", 2 "ti", 3 "ke", 4 "to", 5 "pe", 6 "la", 7 "su"}
   (ctc/day-of-week joda-datetime)))

(defn- time-from [joda-datetime timezone]
  (ctf/unparse (ctf/formatter "HH:mm" timezone) joda-datetime))

(defn- now-in-tz [tz]
  (-> (ctc/now)
      (ctc/to-time-zone tz)))

(defn- add-time-defaults [{:keys [time weekday] :as request} now timezone]
  (let [add-time-and-day (fn [request]
                           (cond (not time) (-> request
                                               (assoc :time (time-from now timezone))
                                               (assoc :weekday (weekday-from now)))
                                 (not weekday) (-> request
                                                   (assoc :weekday (weekday-from now)))
                                 :else request))]
    (-> request
        add-time-and-day)))

(defn- default-bus-count [{:keys [bus-count] :as request}]
  (if-not
      bus-count (assoc request :bus-count 1)
      request))

;;TODO consider moving the logic to add default values outside of this file.
;;     eg. to request.clj.

(defn- add-defaults [request]
  (let [ EET (ctc/time-zone-for-id "Europe/Helsinki")
        now (now-in-tz EET)]
    (-> request
        (add-time-defaults now EET)
        (default-bus-count))))

(defn- filter-needed-keys [request]
  (select-keys request
               [:numbers :from :destination
                :weekday :time :within :bus-count]))

(defn- filter-return-fields [result return-fields]
  (if-not (empty? return-fields)
    (map (fn [result-entry] (select-keys result-entry return-fields)) result)
    result))

;;TODO support alternative :weekday representations (than ma ti ke to pe la su)

(defn buses [{:keys [return-fields] :as request}]
  "The public API function for fetching the buses matching the request"
  (-> request
      add-defaults
      filter-needed-keys
      (buses-for *data*)
      (filter-return-fields return-fields)))
