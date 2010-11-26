(ns clj-coffeescript.compiler
  (:use clj-coffeescript.rhino)
  (:import  [java.io InputStreamReader]))

(defn get-resource [path]
  (.getResourceAsStream (clojure.lang.RT/baseLoader) path))

(defn build-compiler []
  (with-open [compiler-src (get-resource "coffee-script.js")
              compiler-stream (InputStreamReader. compiler-src "UTF-8")]
    (println "CoffeeScript compiler found")
    (with-context [ctx]
      (set-context-interpreted ctx) ;; avoid 64kb src limit
      (let [compiler-scope (build-scope)]
        (.evaluateReader ctx compiler-scope compiler-stream "coffee-script.js" 0 nil)
        (println "compiler loaded")
        compiler-scope))))

(defn compile-string [compiler-scope src & bare?]
  (let [scope (build-scope compiler-scope)]
    (with-context [ctx]
      (.put compiler-scope "coffeeScriptSource" scope src)
      (.evaluateString ctx
                       scope
                       (format "CoffeeScript.compile(coffeeScriptSource, {bare: %s});" bare?)
                       "clj-coffeescript"
                       0 nil))))

(comment "compile coffeescript"
         (use clj-coffeescript.compiler)
         (def compiler-scope (build-compiler))
         (compile-string compiler-scope "a=12;"))