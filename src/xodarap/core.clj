(ns xodarap.core
  (:require [clojure.core.async :refer [go <! <!!]]))

(defonce ^:const rec-prefix "__rec__:")

(defmacro declarec
  "Like `declare`, but used for mutually recursive fns."
  [& names]
  (cons `declare
        (for [name names]
          (symbol (str rec-prefix name)))))

(defn- add-meta
  "merge any key values from opts into the meta data of
  the desired var."
  [name opts]
  (with-meta name (merge (meta name) opts)))

(defn- parse-params
  "Used to handle determining which parameters are
  meant to be docstring or argv. Returns a map of all
  arguments that defrec uses."
  [args]
  (let [second-arg (second args)
        docstring (when (string? second-arg)
                    second-arg)]
    {:name (first args)
     :doc (str docstring)
     :argv (if (vector? second-arg)
             second-arg
             (nth args 2))
     :body (drop (if docstring 3 2) args)}))

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
