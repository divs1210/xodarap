(ns xodarap.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [xodarap.core :refer [letrec rec recfn]]
            [xodarap.test-utils :as u]))

(deftest recfn-test
  (testing "simple recursion"
    (let [fact (recfn f [n]
                 (if (< n 2)
                   1
                   (*' n (rec (f (dec n))))))]
      (is (== (apply *' (range 1 51))
              (fact 50)))
      (is (u/infinity? (fact 1000)))))

  (testing "tail recursion"
    (let [fact-tail (recfn f [acc n]
                      (if (< n 2)
                        acc
                        (recur (*' acc n) (dec n))))]
      (is (== (apply *' (range 1 51))
              (fact-tail 1 50)))
      (is (u/infinity? (fact-tail 1 1000))))))


(deftest letrec-test
  (testing "mutual recursion"
    (letrec [(ODD? [n]
               (if (zero? n)
                 false
                 (rec (EVEN? (dec n)))))
             (EVEN? [n]
               (if (zero? n)
                 true
                 (rec (ODD? (dec n)))))]
            (is (ODD? 10001))
            (is (EVEN? 10000)))))


(deftest defrec-test
  (testing "works as expected"
    (is (== (apply *' (range 1 51))
            (u/fact 50)))
    (is (u/infinity? (u/fact 1000))))

  (testing "docstring is attached"
    (is (= "test fn"
           (:doc (meta #'u/fact)))))

  (testing "marking as private"
    (is (-> #'u/fact meta :private false?))
    (is (-> #'u/private-fact meta :private true?))))
