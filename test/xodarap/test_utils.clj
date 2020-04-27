(ns xodarap.test-utils
  (:require [xodarap.core :refer [defrec defrec- rec]]))

(defn infinity? [x]
  (.isInfinite (double x)))

(defrec fact
  "test fn"
  [n]
  (if (< n 2)
    1
    (*' n (rec (fact (dec n))))))

(defrec- private-fact [n]
  (fact n))
