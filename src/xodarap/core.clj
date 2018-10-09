(ns xodarap.core
  (:require [clojure.core.async :refer [go <! <!!]]))

(defonce ^:const rec-prefix "__rec__:")

(defmacro declarec
  "Like `declare`, but used for mutually recursive fns."
  [& names]
  (cons `declare
        (for [name names]
          (symbol (str rec-prefix name)))))

(defmacro defrec
  "Like `defn`, but defines a recursive fn that
  doesn't blow the stack iff recursive calls are
  wrapped in `rec`."
  [name argv & body]
  (let [internal-name (symbol (str rec-prefix name))]
    `(do (defn ~internal-name ~argv
           (go
             ~@body))
         (defn ~name ~argv
           (<!! (~internal-name ~@argv))))))

(letfn [(internal-sym-name [sym]
          (let [ns (namespace sym)
                n (name sym)]
            (symbol
             (if ns
               (str ns "/" rec-prefix n)
               (str rec-prefix n)))))]
  (defmacro rec
    "Converts a stack-blowing recursive call into a safe one.
  Use with `defrec`."
    [expr]
    (let [[op & args] expr]
      `(<! ~(cons (internal-sym-name op) args)))))
