(ns clj-coffeescript.compiler
  (:use clj-coffeescript.rhino))

(defn build-compiler []
  (with-context [ctx]
    (set-context-interpreted ctx) ;; avoid 64kb src limit
    (let [compiler-scope (build-scope)]
      ;; load the coffeescript compiler
      (load-resources ["coffee-script.js"] compiler-scope ctx)
      compiler-scope)))

(defn compile-string [compiler-scope src & bare?]
  (let [scope (build-scope compiler-scope)]
    (with-context [ctx]
      ;; load the script to compile into a variable
      (set-named-property "coffeeScriptSource" src compiler-scope scope)
      ;; compile the contents of the variable
      (evaluate-string (format "CoffeeScript.compile(coffeeScriptSource, {bare: %s});" bare?)
                       "clj-coffeescript"
                       scope
                       ctx))))

(comment "compile coffeescript"
         (use clj-coffeescript.compiler)
         (def compiler-scope (build-compiler))
         (compile-string compiler-scope "a=12;"))