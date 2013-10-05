(ns bus-jkl.request
  (:require [bus-jkl.util :as util]
            [clojure.string :as s]))

(defn drop-middle [string]
  (str (first string) (last string)))

(defn vec-or-list? [string]
  (#{"[]" "()"} (drop-middle string)))

(defn components [seq-str]
  (s/split seq-str #","))

(defn drop-parens [string]
  (if (vec-or-list? string)
    (->> string
         rest
         butlast
         (apply str))
    string))

(defn remove-whitespace [string]
  (s/replace string #"\s" ""))

(defn int-str? [string]
  (util/parse-int string))

(defn parse-seq [string]
  (->> string
       remove-whitespace
       drop-parens
       components
       (filter int-str?)
       (map util/parse-int)))

(defn line-numbers-to-seq [{:keys [numbers] :as params}]
  (if numbers
    (assoc params :numbers (parse-seq numbers))
    params))
