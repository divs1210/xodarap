(ns xodarap.macro-utils)

(defn add-meta
  "Merge any key values from opts into the meta data of
  the desired var."
  [name opts]
  (with-meta name (merge (meta name) opts)))

(defn parse-params
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
