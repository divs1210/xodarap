# xodarap
[![Build Status](https://travis-ci.com/divs1210/xodarap.svg?branch=master)](https://travis-ci.com/divs1210/xodarap)
[![Clojars Project](https://img.shields.io/clojars/v/xodarap.svg)](https://clojars.org/xodarap)

Fearless recursion in Clojure!

## Why

The JVM puts a hard limit on how deep our functions can recurse without blowing the stack:

```clojure
;; Ex. 1
;; =====
(defn unsafe-factorial [n]
  (if (< n 2)
    1
    (*' n (unsafe-factorial (dec n)))))

(unsafe-factorial 5000)
;; => StackOverflowError   clojure.lang.Numbers.lt (Numbers.java:3816)
```

This is a very simple example, and the fn can be defined in a tail-recursive manner to avoid this issue,
but we are not always this lucky! Consider:

```clojure
;; Ex. 2
;; =====
(defn unsafe-ackermann [m n] 
  (cond (zero? m) (inc n)
        (zero? n) (recur (dec m) 1)
        :else (recur (dec m) (unsafe-ackermann m (dec n)))))

(unsafe-ackermann 3 10)
;; => StackOverflowError   clojure.lang.Numbers$LongOps.inc (Numbers.java:545)
```
Try converting the above to use tail recursion - possible, but not trivial.

... and then there are mutually-recursive functions:

```clojure
;; Ex. 3
;; =====
(declare is-even?)

(defn is-odd? [n]
  (if (zero? n)
    false
    (is-even? (dec n))))

(defn is-even? [n]
  (if (zero? n)
    true
    (is-odd? (dec n))))

(is-even? 10000)
;; => StackOverflowError   clojure.lang.Numbers$LongOps.isZero (Numbers.java:443)
```
Clojure's `recur` form doesn't help us here, so we generally end up using a [trampoline](https://clojuredocs.org/clojure.core/trampoline).

Is there a general way to recurse safely (ie without blowing up the stack)
in all these cases, and without changing the structure of our code?

## Usage

```clojure
(use 'xodarap.core)

;; Ex. 1
;; =====
(defrec factorial [n]
  (if (< n 2)
    1
    (*' n (rec (factorial (dec n))))))

(factorial 5000)
;; => 4228577926605543522201064200233584405390...
```

Here, we define a safe recursive factorial fn using the `defrec` form. Note that the
recursive call to itself is wrapped in a `rec` form.

```clojure
;; Ex. 2
;; =====
(defrec ackermann [m n] 
  (cond (zero? m) (inc n)
        (zero? n) (recur (dec m) 1)
        :else (recur (dec m) (rec (ackermann m (dec n))))))

(ackermann 3 10)
;; => 8189 (after a long pause)
```

We can crack tougher nuts using the same method.

```clojure
;; Ex. 3
;; =====
(letrec [(is-odd? [n]
           (if (zero? n)
             false
             (rec (is-even? (dec n)))))
         (is-even? [n]
           (if (zero? n)
             true
             (rec (is-odd? (dec n)))))]
  (is-even? 10000))
;; => true
```

Here we define mutually recursive functions using `letrec`.

```clojure
;; like fn
(recfn fact [n]
  (if (< n 2)
    1
    (*' n (rec (fact (dec n))))))
```

Similarly, `recfn` is an alternative to `fn`.

## License

Copyright © 2018 Divyansh Prakash

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
