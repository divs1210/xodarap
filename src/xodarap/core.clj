(ns xodarap.core
  (:require [clojure.core.async :refer [<! <!! go-loop]]
            [xodarap.macro-utils :refer [add-meta parse-params]]))

(defonce ^:const rec-prefix "__rec__:")

(defmacro letrec
  "Like `letfn`, but defines recursive fns that
  don't blow the stack iff recursive calls are
  wrapped in `rec`.
  Tail calls can use `recur` normally."
  [fnspecs & body]
  `(letfn [~@(for [[name argv & body] fnspecs
                   :let [internal-name (symbol (str rec-prefix name))
                         duped-bindings (for [arg argv
                                              arg [arg arg]]
                                          arg)]
                   a-def `[(~internal-name ~argv
                             (go-loop [~@duped-bindings]
                               ~@body))
                           (~name ~argv
                             (<!! (~internal-name ~@argv)))]]
               a-def)]
     ~@body))

(defmacro recfn
  "Like `fn`, but defines a recursive fn that
  doesn't blow the stack iff recursive calls are
  wrapped in `rec`.
  Tail calls can use `recur` normally."
  [name argv & body]
  `(letrec [(~name ~argv ~@body)]
     ~name))

(defn- defrec*
  [args & [private?]]
  (let [{:keys [name doc argv body]} (parse-params args)
        meta-data {:doc doc
                   :private (boolean private?)}]
    `(def ~(add-meta name meta-data)
       (recfn ~name ~argv
          ~@body))))

(defmacro defrec
  "Like `defn`, but defines a recursive fn that
  doesn't blow the stack iff recursive calls are
  wrapped in `rec`.
  Tail calls can use `recur` normally."
  [& args]
  (defrec* args))

(defmacro defrec-
  "Like `defn`, but defines a private recursive fn
  that doesn't blow the stack iff recursive calls
  are wrapped in `rec`.
  Tail calls can use `recur` normally."
  [& args]
  (defrec* args true))

(letfn [(internal-sym-name [sym]
          (let [ns (namespace sym)
                n (name sym)]
            (symbol
              (if ns
                (str ns "/" rec-prefix n)
                (str rec-prefix n)))))]
  (defmacro rec
    "Converts a stack-blowing recursive call into a safe one.
    Use with `recfn` or `letrec` or `defrec`."
    [expr]
    (let [[op & args] expr]
      `(<! ~(cons (internal-sym-name op) args)))))

(defmacro ^{:deprecated "0.2.1"
            :doc "`declarec` has been deprecated! Use `letrec` instead."}
  declarec
  [& _]
  (throw (Exception. "`declarec` has been deprecated! Use `letrec` instead.")))
