(ns bus-jkl.request-test
  (:use bus-jkl.request
        clojure.test
        midje.sweet))

;;-----------------------------------------------
;; line-numbers-to-sec
;;-----------------------------------------------

(fact "Request has no numbers. Returns the map untouched"
  (line-numbers-to-seq {}) => {})

(fact "Request has empty numbers. Returns an empty number seq"
  (line-numbers-to-seq {:numbers ""}) => {:numbers []}
  (line-numbers-to-seq {:numbers " "}) => {:numbers []}
  (line-numbers-to-seq {:numbers "[]"}) => {:numbers []}
  (line-numbers-to-seq {:numbers "()"}) => {:numbers []})

(fact "Request has one line number x as string. Returns the map with :numbers [x] where x converted to str"
  (line-numbers-to-seq {:numbers "9"}) => {:numbers ["9"]}
  (line-numbers-to-seq {:numbers "9c"}) => {:numbers ["9c"]}
  (line-numbers-to-seq {:numbers " 9 "}) => {:numbers ["9"]}
  (line-numbers-to-seq {:numbers "[9]"}) => {:numbers ["9"]}
  (line-numbers-to-seq {:numbers " [ 9 ] "}) => {:numbers ["9"]}
  (line-numbers-to-seq {:numbers " [ 8 9 ] "}) => {:numbers ["89"]})

(fact "Request has several numbers as string. Returns the map with items converted to seq as numbers"
  (line-numbers-to-seq {:numbers "9,10"}) => {:numbers ["9" "10"]}
  (line-numbers-to-seq {:numbers "(9,10)"}) => {:numbers ["9" "10"]}
  (line-numbers-to-seq {:numbers "[9,10]"}) => {:numbers ["9" "10"]}
  (line-numbers-to-seq {:numbers "[ 1 1 , 2 2 ]"}) => {:numbers ["11" "22"]})

;;-----------------------------------------------
;; bus-count-to-int
;;-----------------------------------------------

(fact "Req params doesn't have a value for bus-count. Request is returned unchanged"
  (bus-count-to-int {:foo "foo"}) => {:foo "foo"}
  (bus-count-to-int {:foo "foo", :bus-count nil}) => {:foo "foo", :bus-count nil})

(fact "Req params has a valid bus-count as a string value. Req returned with buscount converted to int"
  (bus-count-to-int {:bus-count "2"}) => {:bus-count 2})
