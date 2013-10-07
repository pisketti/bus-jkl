(ns bus-jkl.request
  (:require [bus-jkl.util :as util]
            [clojure.string :as s]))

(defn- drop-middle [string]
  (str (first string) (last string)))

(defn- vec-or-list? [string]
  (#{"[]" "()"} (drop-middle string)))

(defn- components [seq-str]
  (s/split seq-str #","))

(defn- drop-parens [string]
  (if (vec-or-list? string)
    (->> string
         rest
         butlast
         (apply str))
    string))

(defn- remove-whitespace [string]
  (s/replace string #"\s" ""))

(defn- int-str? [string]
  (util/parse-int string))

(defn- parse-seq [string]
  (->> string
       remove-whitespace
       drop-parens
       components
       (remove empty?)))

;; Functions that transform the request params.
;; Like ring handlers but only for the params

(defn line-numbers-to-seq [{:keys [numbers] :as params}]
  (if numbers
    (assoc params :numbers (parse-seq numbers))
    params))

(defn bus-count-to-int [{:keys [bus-count] :as params}]
  (if bus-count
    (assoc params :bus-count (util/parse-int bus-count))
    params))

(defn pret [msg x]
  (println msg)
  x)

(defn return-fields-to-seq [{:keys [return-fields] :as params}]
  (if return-fields
    (assoc params :return-fields (->> return-fields
                                      parse-seq
                                      (map keyword)))
    params))
