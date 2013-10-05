(ns bus-jkl.request
  (:require [bus-jkl.util :as util]))

(defn line-numbers-to-seq[{:keys [numbers] :as params}]
  (if numbers
    (assoc params :numbers (util/parse-int numbers))
    params))
