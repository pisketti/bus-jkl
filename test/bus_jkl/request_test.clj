(ns bus-jkl.request-test
  (:use bus-jkl.request
        clojure.test
        midje.sweet))

;;-----------------------------------------------
;; line-numbers-to-sec
;;-----------------------------------------------

(fact "Request has no numbers. Returns the map untouched"
  (line-numbers-to-seq {}) => {})

(fact "Request has one number x as string. Returns the map with :numbers [x] where x converted to str"
  (line-numbers-to-seq {:numbers "9"}) => {:numbers 9}
  ;(line-numbers-to-seq {:numbers "[9]"}) => {:numbers 9}
  )
