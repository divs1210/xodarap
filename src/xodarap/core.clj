(ns xodarap.core
  (:require [clojure.core.async :refer [<! <!! go]]
            [xodarap.macro-utils :refer [add-meta parse-params]]))

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
  [& args]
  (let [{:keys [name doc argv body]} (parse-params args)
        internal-name (symbol (str rec-prefix name))
        meta-data {:doc doc}]
    `(do
       (defn ~(add-meta internal-name meta-data)
         ~argv
         (go ~@body))
       (defn ~(add-meta name meta-data)
         ~argv
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
