(ns clj-coffeescript
  ( :import  [org.jcoffeescript
              JCoffeeScriptCompiler
              JCoffeeScriptCompileException]))

(defonce *compiler* (JCoffeeScriptCompiler.))

(defn compile-string [src]
  (try
    (.compile *compiler* src)
    (catch JCoffeeScriptCompileException e
      (println "do something here!" e))))

(defn compile-file [src-file-path dst-file-path]
  (let [src (slurp src-file-path)
        dst (compile-string src)]
    (println dst)
    (spit dst-file-path dst)))


