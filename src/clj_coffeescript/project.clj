(ns clj-coffeescript.project
  "Manage coffescript and javascript projects"
  (:require
   [clj-coffeescript.compiler :as compiler]
   [clj-coffeescript.rhino :as rhino]))

(defn project
  "Build a project from a list of components"
  [& {:keys [coffee-path javascript-path
             coffee-test-path javascript-test-path
             dependencies]
      :as options}]
  options)

(defmacro coffeescript [name & options]
  `(hash-map :name '~name ~@options))

(defn test-all
  [project]
  (when-let [coffee-test-path (:coffee-test-path project)]
    (let [compiler (compiler/build-compiler)]
      (doseq [f (file-seq (java.io.File . coffee-test-path))]
        (when (.isFile f)
          (println (.getPath f))
          (let [js (compiler/compile-string compiler (slurp f))
                scope (rhino/build-scope)]
            ;; TODO: add a require implementation here
            (rhino/evaluate-string js (.getPath f) scope)))))))
