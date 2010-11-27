(ns clj-coffeescript.shell
  (:require [clj-coffeescript.rhino :as rhino]
            [clj-coffeescript.compiler :as compiler]))

(def *compiler* (compiler/build-compiler))

(def *runtime* )

(defmacro with-new-scope [& body]
  `(binding [*runtime* (rhino/build-scope)]
    ~@body))

(defmacro with-scope [[scope] & body]
  `(binding [*runtime* ~scope]
    ~@body))

(defn new-scope []
  (rhino/build-scope))

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