(ns clj-coffeescript
  ( :import  [java.io InputStreamReader]
             [org.mozilla.javascript Context]))

(defn get-resource [path]
  (.getResourceAsStream (clojure.lang.RT/baseLoader) path)) 

(defn build-compiler []
  (with-open [compiler-src (get-resource "coffee-script.js")
              compiler-stream (InputStreamReader. compiler-src "UTF-8")]
    (println "compiler found")
    (let [context (Context/enter)]
      (.setOptimizationLevel context -1)
      (println "context created" context)
      (try (let [globalscope (.initStandardObjects context)]
             (println "global scope created" globalscope)
             (.evaluateReader context globalscope compiler-stream "coffee-script.js" 0 nil)
             (println "compiler loaded")
             globalscope)
           (finally (println "aaaaand I'm done")
                    (Context/exit))))))

(defn compile-string [global-scope src & bare?]
  (let [context (Context/enter)
        compile-scope (.newObject context global-scope)]
    (try
      (.setParentScope compile-scope global-scope)
      (.put compile-scope "coffeeScriptSource" compile-scope src)
      (.evaluateString context
                       compile-scope
                       (format "CoffeeScript.compile(coffeeScriptSource, {bare: %s});" bare?)
                       "clj-coffeescript"
                       0 nil)
      (finally (Context/exit)))))