(ns clj-coffeescript.compiler
  (:use clj-coffeescript.rhino)
  (:import  [java.io InputStreamReader]))

(defn get-resource [uri]
  (.getResourceAsStream (clojure.lang.RT/baseLoader) uri))

(defn get-resource-as-stream [uri]
  (let [resource (get-resource uri)]
    (InputStreamReader. resource "UTF-8")))

(defn build-compiler []
  (with-open [compiler (get-resource-as-stream "coffee-script.js")]
    (with-context [ctx]
      (set-context-interpreted ctx) ;; avoid 64kb src limit
      (let [compiler-scope (build-scope)]
        (load-stream compiler "coffee-script.js" compiler-scope ctx)
        compiler-scope))))

(defn compile-string [compiler-scope src & bare?]
  (let [scope (build-scope compiler-scope)]
    (with-context [ctx]
      (set-named-property "coffeeScriptSource" src compiler-scope scope)
      (evaluate-string (format "CoffeeScript.compile(coffeeScriptSource, {bare: %s});" bare?)
                       "clj-coffeescript"
                       scope
                       ctx))))

(comment "compile coffeescript"
         (use clj-coffeescript.compiler)
         (def compiler-scope (build-compiler))
         (compile-string compiler-scope "a=12;"))