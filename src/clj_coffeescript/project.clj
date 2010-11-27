(ns clj-coffeescript.project
  "Manage coffescript and javascript projects"
  (:require
   [clj-coffeescript.compiler :as compiler]
   [clj-coffeescript.rhino :as rhino]
   [clojure.java.io :as io]))

(defn project
  "Build a project from a list of components"
  [& {:keys [coffee-path javascript-path
             coffee-test-path javascript-test-path
             dependencies]
      :as options}]
  options)

(defmacro coffeescript [name & options]
  `(hash-map :name '~name ~@options))

(defn file-with-extension [path & extensions]
  (some
   #(let [file (io/file (str path %))]
      (and (.isFile file) [file %]))
   extensions))

(defn js-evaluate
  [compiler scope ns js]
  (let [scope (rhino/build-scope scope)]
    (.put scope "exports" scope nil)
    (rhino/evaluate-string "exports={};" ns scope)
    (rhino/evaluate-string js ns scope)
    (.get scope "exports" scope)))

(defn js-require*
  [project compiler scope ns]
  (let [[file extension] (file-with-extension
                           (io/file (:coffee-path project) ns)
                           ".js" ".coffee")]
    (case extension
      ".js" (js-evaluate compiler scope ns (slurp file))
      ".coffee" (js-evaluate
                 compiler scope ns
                 (compiler/compile-string compiler (slurp file)))
      (throw (java.io.FileNotFoundException. ns)))))

(def *project*)
(def *compiler*)
(def *scope*)

(defn js-require [ns]
  (js-require* *project* *compiler* *scope* ns))

(def js-require-def
  "function require(ns) {
     var rt_var=Packages.clojure.lang.RT['var'];
     return rt_var('clj-coffeescript.project','js-require').invoke(ns);
   }")

(defn test-all
  [project]
  (when-let [coffee-test-path (:coffee-test-path project)]
    (let [compiler (compiler/build-compiler)]
      (doseq [f (file-seq (java.io.File. coffee-test-path))]
        (when (.isFile f)
          (let [js (compiler/compile-string compiler (slurp f))
                scope (rhino/build-scope)]
            (binding [*project* project
                      *compiler* compiler
                      *scope* scope]
              (rhino/evaluate-string js-require-def "require" scope)
              (rhino/evaluate-string js (.getPath f) scope))))))))
