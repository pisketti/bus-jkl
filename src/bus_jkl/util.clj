(ns bus-jkl.util)

(defn parse-int [str]
  (try
    (Integer. (re-find #"[0-9]*" str))
    (catch Exception e nil)))
