(ns clj-coffeescript.compiler
  (:require [clj-coffeescript.rhino :as rhino]))

(defn build-compiler []
  (rhino/with-context [ctx]
    (rhino/set-context-interpreted ctx) ;; avoid 64kb src limit
    (let [compiler-scope (rhino/build-scope)]
      ;; load the coffeescript compiler
      (rhino/load-resources ["resources/coffee-script.js"] compiler-scope ctx)
      compiler-scope)))

(defn compile-string [compiler-scope src & bare?]
  (let [scope (rhino/build-scope compiler-scope)]
    (rhino/with-context [ctx]
      ;; load the script to compile into a variable
      (rhino/set-named-property "coffeeScriptSource" src compiler-scope scope)
      ;; compile the contents of the variable
      (rhino/evaluate-string (format "CoffeeScript.compile(coffeeScriptSource, {bare: %s});" bare?)
                       "clj-coffeescript"
                       scope
                       ctx))))

(defn evaluate-string [compiler-scope runtime-scope src]
  (let [js (compile-string compiler-scope src true)]
    (rhino/with-context [ctx]
      (rhino/evaluate-string js "compiled coffee" runtime-scope))))

(comment "compile coffeescript"
         (use clj-coffeescript.compiler)
         (def compiler (build-compiler))
         (compile-string compiler "a=12;")
         (evaluate-string compiler "a=3+4"))