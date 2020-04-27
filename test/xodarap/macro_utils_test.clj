(ns xodarap.macro-utils-test
  (:require [clojure.test :refer :all]
            [xodarap.macro-utils :as u]))

(deftest parse-params-test
  (is (= {:name 'foo
          :doc "Docstring"
          :argv ['a 'b]
          :body (list + 'a 'b)}
         (u/parse-params ['foo "Docstring" ['a 'b] + 'a 'b])))
  (is (= {:name 'foo
          :doc ""
          :argv ['a 'b]
          :body (list + 'a 'b)}
         (u/parse-params ['foo ['a 'b] + 'a 'b]))))
