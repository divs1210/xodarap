(ns xodarap.core
  (:require [clojure.core.async :refer [go <! <!!]]))

(letfn [(internal-sym-name [sym]
          (let [ns (namespace sym)
                n (name sym)]
            (symbol
             (if ns
               (str ns "/rec:" n)
               (str "rec:" n)))))]
  (defmacro rec [[op & args]]
    `(<! ~(cons (internal-sym-name op) args))))

(defmacro defrec [name argv & body]
  (let [internal-name (symbol (str "rec:" name))]
    `(do (defn ~internal-name ~argv
           (go
             ~@body))
         (defn ~name ~argv
           (<!! (~internal-name ~@argv))))))
