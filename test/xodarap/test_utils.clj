(ns xodarap.test-utils
  (:require [xodarap.core :refer [defrec defrec- rec]]))

(defrec fact
  "test fn"
  [n]
  (if (< n 2)
    1
    (*' n (rec (fact (dec n))))))

(defrec- private-fact [n]
  (fact n))
