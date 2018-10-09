(ns xodarap.core-test
  (:require [clojure.test :refer :all]
            [xodarap.core :refer :all]))

;; Simple Recursion
;; ================
(defrec fact [n]
  (if (< n 2)
    n
    (*' n (rec (fact (dec n))))))

(deftest simple-recursion-test
  (is (== (apply *' (range 1 5001))
          (fact 5000))))


;; Mutual Recursion
;; ================
(declare EVEN? rec:EVEN?)

(defrec ODD? [n]
  (if (zero? n)
    false
    (rec (EVEN? (dec n)))))

(defrec EVEN? [n]
  (if (zero? n)
    true
    (rec (ODD? (dec n)))))

(deftest mutual-recursion-test
  (is (ODD? 5001))
  (is (EVEN? 5000)))
