(ns bus-jkl.core
  (:use [clojure.string :only [split upper-case]]
        [clojure.pprint :only [pprint]])
  (:require [bus-jkl.data :as data]))

(def ^:dynamic *data* (data/read-data))

(defn- one-of? [str candidates]
  (some (fn [candidate] (= (upper-case str) (upper-case candidate))) candidates))

(defn- filter-by-numbers [numbers lines]
  (filter (fn [line]
            (if numbers
              ((set numbers) (:number line))
              true))
          lines))

(defn- filter-by-from-centre [from-centre lines]
  (filter (fn [{:keys [title] :as line}]
            (let [line-from-centre (one-of? (first title)
                                            ["keskusta" "kauppatori"])]
              (cond
               (true? from-centre) line-from-centre
               (false? from-centre) (not line-from-centre)
               :else true))) ;; do not drop lines if from-centre not set
          lines))

(defn- filter-by-destination [destination lines]
  "Filters lines by destination. Checks title, districts and route.
   Skips the first in each since destination cannot be where the travelling starts."
  (if-not destination
    lines
    (filter
     (fn [{:keys [title districts route]}]
       (or (one-of? destination (rest title))
           (one-of? destination (rest districts))
           (one-of? destination (rest (map :stop route)))))
     lines)))

;;TODO: can be removed. For debugging only
(defn prn-ret [msg returnable]
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


(defn- lines-for [{:keys [numbers from-centre destination weekday] :as request} data]
  (->> data
       (filter-by-numbers numbers)
       (filter-by-from-centre from-centre)
       (filter-by-destination destination)
       (filter-by-weekday weekday)))

(defn- parse-int [str]
  (try
    (Integer. (re-find #"[0-9]*" str))
    (catch Exception e nil)))

(defn- mins-from-midnight [time-str]
  (let [[hours-str mins-str] (split time-str #":")
        mins (parse-int mins-str)
        hours (parse-int hours-str)]
    (if (and mins hours)
      (+ (* 60 hours) mins))))

(defn- earlier-than [max-time]
  "Returns a function that checks whether a given time is before the max time"
  (let [max-mins (mins-from-midnight max-time)]
    (fn [{:keys [time]}]
      (< (mins-from-midnight time) max-mins))))

(defn- within-time [now within-mins]
  (let [max-mins (+ (mins-from-midnight now) within-mins)]
    (fn [{:keys [time]}]
      (< (mins-from-midnight time) max-mins))))

(defn limit-by-count [count buses]
  (if count (take count buses) buses))

(defn- times-from-line [{:keys [time bus-count within]} line-data]
  "Filteres the lines that match the request from all lines"
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

(defn- buses-for [{:keys [weekday buscount] :as request} lines]
  (->> lines
       (lines-for request)
       (buses-from-lines request)
       flatten
       sort-by-time
       (limit-by-count buscount)
       ;;(prn-ret "\nresult: ")
       ))

;; The public API function

(defn buses [request]
  (buses-for (select-keys request
                          [:numbers :from-centre :destination :weekday :time :within :bus-count])
             *data*))
