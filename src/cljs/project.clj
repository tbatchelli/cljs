(ns cljs.project
  "Manage coffescript and javascript projects"
  (:require
   [cljs.compiler :as compiler]
   [cljs.rhino :as rhino]
   [clojure.zip :as zip]
   [clojure.string :as string]
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





(defn in-result
  "Check to see if the candidate tree is already in the "
  [result candidate]
  )

(defn reduce-dependency-trees
  "Take the per file trees and reduce them as much as possible."
  [trees]
  (reduce
   #(if (in-result %1 %2) %1 (conj %1 %2))
   []
   trees))


;;; Define a javascript require function that will load the project files
(defn- memoize-require
  "Returns a memoized version of the function. The memoized version of the
  function keeps a cache of the mapping from all arguments but the last to
  results and, when calls with the same arguments are repeated often, has higher
  performance at the expense of higher memory use."
  {:added "1.0"} [f]
  (let [mem (atom {})]
    (fn [& args]
      (let [discriminator (butlast args)
            e (find @mem discriminator)
            l (last args)]
        (if (and e (= (second (val e)) l))
          (first (val e))
          (let [ret (apply f args)]
            (swap! mem assoc discriminator [ret l])
            ret))))))

(def ^{:private true :doc "Load a required file."}
  load-require
  (memoize-require
   (fn load-require*
     [scope ns js]
     (let [scope (rhino/build-scope scope)]
       (.put scope "exports" scope nil)
       (rhino/evaluate-string "exports={};" ns scope)
       (rhino/evaluate-string js ns scope)
       {:exports (.get scope "exports" scope)
        :js js}))))

(defn- file-with-extension
  "Return the first existing file that matches a base path and a list of
  extensions."
  [path-extensions]
  (some
   #(let [file (io/file (first %))]
      (and (.isFile file) %))
   path-extensions))

(defn- find-file
  "Find a js or coffee file in the project's paths"
  [project ns]
  (or
   (file-with-extension
     [[(io/file (:coffee-path project) (str ns ".coffee")) :coffee]
      [(io/file (:javascript-path project) (str ns ".js")) :js]])
   (throw (java.io.FileNotFoundException. ns))))

;; TODO multimethod
(defn js-require*
  [project scope ns file extension]
  (case extension
    :js (load-require scope ns (slurp file))
    :coffee (load-require
             scope ns
             (compiler/compile-string (:compiler project) (slurp file)))))

;; because we build the dependencies through a javascript call
;; we provide context using sepecial variables.
(def *project*)
(def *scope*)
(def *require-stack*)

(defn js-require
  "The impl of the js require function."
  [ns]
  (let [[file extension] (find-file *project* ns)]
    (or
     (if-let [namespace (-> *project* :namespaces (get file))]
       (:exports namespace))
     (let [namespace (js-require*
                      *project* *scope* ns file extension)]
       (set! *project* (assoc-in *project* [:namespaces file] namespace))
       (:exports namespace)))))

(defn js-tracking-require
  "The impl of the js require function."
  [ns]
  (let [[file extension] (find-file *project* ns)]
    (or
     (if-let [namespace (-> *project* :namespaces (get file))]
       (when-let [deps (some
                        (fn find-root-dependency [tree]
                          (and (= file (first tree)) tree))
                        (:dependencies *project*))]
         ;; the dependency is a root in the dependencies list,
         ;; so wee clobber the root and add it here
         (set! *project*
               (update-in *project* [:dependencies]
                          (fn [deps] (remove #(= file (first %)) deps))))
         (set!
          *require-stack*
          (zip/append-child *require-stack* deps)))
       (:exports namespace))
     (try
       (set!
        *require-stack*
        (-> *require-stack*
            (zip/append-child file)
            zip/down
            zip/rightmost))
       (let [namespace (js-require*
                        *project* *scope* ns file extension)]
         (set! *project* (assoc-in *project* [:namespaces file] namespace))
         (:exports namespace))
       (finally
        (set! *require-stack* (zip/up *require-stack*)))))))

(def js-require-def
  "function require(ns) {
     var rt_var=Packages.clojure.lang.RT['var'];
     return rt_var('cljs.project','js-require').invoke(ns);
   }")

(def js-tracking-require-def
  "function require(ns) {
     var rt_var=Packages.clojure.lang.RT['var'];
     return rt_var('cljs.project','js-tracking-require').invoke(ns);
   }")


(defn to-root
  "zips all the way up and returns the root node."
  [loc]
  (if (= :end (loc 1))
    loc
    (let [p (zip/up loc)]
      (if p
        (recur p)
        loc))))

(defn remove-requires
  "Remove the dependency mechanism from the source"
  [js]
  (string/replace js #"\s*[a-zA-Z0-9_]+\s*=\s*exports.*" ""))

(defn js-dependencies
  "Calcuate js dependencies for given file."
  [project f js]
  (let [scope (rhino/build-scope)]
    ;; put our state into bindings, and set up some state for js evaluation
    (binding [*project* project
              *scope* scope
              *require-stack* (zip/vector-zip [f])]
      (rhino/evaluate-string js-tracking-require-def "require" scope)
      (rhino/evaluate-string "exports={};" "def exports" scope)
      (rhino/evaluate-string js (.getPath f) scope)
      ;; recover state from bindings
      (->
       *project*
       (update-in [:dependencies] conj (zip/root *require-stack*))
       (assoc-in [:namespaces f :js] js)))))

(defn js-run
  "Run js file."
  [project f js]
  (let [scope (rhino/build-scope)]
    ;; put our state into bindings, and set up some state for js evaluation
    (binding [*project* project
              *scope* scope]
      (rhino/evaluate-string js-require-def "require" scope)
      (rhino/evaluate-string js (.getPath f) scope)
      (assoc-in *project* [:namespaces f :js] js))))

(defn coffee->js
  "Compile to js"
  [compiler f]
  (compiler/compile-string compiler (slurp f)))

(defn postwalk-tree-seq
  [branch? children root]
  (let [walk (fn walk [node]
               (lazy-seq
                (if (branch? node)
                  (concat
                   (mapcat walk (reverse (children node)))
                   (cons node nil))
                  (cons node nil))))]
    (walk root)))


;;; Testing
(defn test-all
  [project]
  (when-let [path (:coffee-test-path project)]
    (let [compiler (compiler/build-compiler)
          project (assoc project :compiler compiler)]
      (reduce
       (fn test-all-run-test [project f]
         (let [js (compiler/compile-string compiler (slurp f))
               scope (rhino/build-scope)]
           (js-run project f js)))
       project
       (filter #(.isFile %) (file-seq (java.io.File. path)))))))


(defn combine-all
  "Build a single javascript that is suitable for serving over a web
   request."
  [project]
  (when-let [coffee-path (:coffee-path project)]
    (let [project (reduce
                   #(if (-> %1 :namespaces (get %2))
                      %1
                      (->>
                       (coffee->js (-> %1 :compiler) %2)
                       (js-dependencies %1 %2)))
                   (assoc project
                     :namespaces nil
                     :compiler (compiler/build-compiler))
                   (filter
                    #(.isFile %)
                    (file-seq (java.io.File. coffee-path))))]
      (remove-requires
       (reduce
        #(str
          %1
          (string/join
           \newline
           (map
            (fn source-for [f] (-> project :namespaces (get f) :js))
            (filter (complement vector?) (postwalk-tree-seq vector? seq %2)))))
        ""
        (:dependencies project))))))
