(ns clj-coffeescript.shell
  (:require [clj-coffeescript.rhino :as rhino]
            [clj-coffeescript.compiler :as compiler]))

(def *compiler* (compiler/build-compiler))

(def *runtime* nil)

(defmacro with-new-scope [& body]
  `(binding [*runtime* (rhino/build-scope)]
    ~@body))

(defmacro with-scope [[scope] & body]
  `(binding [*runtime* ~scope]
    ~@body))

(defn new-scope [& [parent]]
  (if parent
    (rhino/build-scope parent)
    (rhino/build-scope)))

(defn cs
  ([script]
     (compiler/evaluate-string *compiler* *runtime* script))
  ([script scope]
     (compiler/evaluate-string *compiler* scope script)))

(defn js
  ([script]
     (rhino/evaluate-string script "shell" *runtime*))
  ([script scope]
     (rhino/evaluate-string script "shell" scope)))

(defn set-scope [scope]
  (alter-var-root #'*runtime* (fn [_] scope)))