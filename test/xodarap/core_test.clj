(ns xodarap.core-test
  (:require [clojure.test :refer :all]
            [xodarap.core :refer [declarec defrec rec]]
            [xodarap.private-fn :as private-fn]))

;; Simple Recursion
;; ================
(defrec fact [n]
  (if (< n 2)
    1
    (*' n (rec (fact (dec n))))))

(deftest simple-recursion-test
  (is (== (apply *' (range 1 51))
          (fact 50))))


;; Tail Recursion
;; ==============
(defrec fact-tail [f n]
  (if (< n 2)
    f
    (recur (*' f n) (dec n))))

(deftest tail-recursion-test
  (is (== (apply *' (range 1 51))
          (fact-tail 1 50))))


;; Mutual Recursion
;; ================
(declarec EVEN?)

(defrec ODD? [n]
  (if (zero? n)
    false
    (rec (EVEN? (dec n)))))

(defrec EVEN? [n]
  (if (zero? n)
    true
    (rec (ODD? (dec n)))))

(deftest mutual-recursion-test
  (is (ODD? 10001))
  (is (EVEN? 10000)))


;; Docstring Support
;; =================
(defrec f-no-docstring
  [x]
  (if (zero? x)
    true
    (rec (f-no-docstring (dec x)))))

(defrec f-docstring
  "This is my docstring!"
  [x]
  (if (zero? x)
    true
    (rec (f-docstring (dec x)))))

(deftest docstring-test
  (is (= true
         (f-docstring 1000)
         (f-no-docstring 1000)))
  (is (= "This is my docstring!"
         (:doc (meta #'f-docstring))))
  (is (= ""
         (:doc (meta #'f-no-docstring)))))


;; Private function definition
;; ===========================
(deftest private-defspec-test
  (is (= 120
         (private-fn/public-recursive 5)
         (#'private-fn/private-recursive 5))
      "Test that private fn acts the same as the public version.")
  (testing "Check the value of private in function meta data"
    (is (false? (-> private-fn/public-recursive var meta :private)))
    (is (true? (-> private-fn/private-recursive var meta :private)))))
