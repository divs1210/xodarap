(ns xodarap.private-fn
  (:require [xodarap.core :refer [defrec defrec- rec]]))

(defrec public-recursive
  [n]
  (if (zero? n)
    1
    (* n (rec (public-recursive (dec n))))))

(defrec- private-recursive
  [n]
  (if (zero? n)
    1
    (* n (rec (private-recursive (dec n))))))
