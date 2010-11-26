(ns clj-coffeescript.rhino
  (:import [org.mozilla.javascript Context]))

(defmacro with-context [[ctx] & body]
  `(try (let [~ctx (Context/enter)]
          ~@body)
        (finally (Context/exit))))

(defn set-context-interpreted [ctx]
  (.setOptimizationLevel ctx -1))

(defn build-scope
  ([]
     (with-context [ctx]
       (let [new-scope (.initStandardObjects ctx)] 
         new-scope)))
  ([parent]
     (with-context [ctx]
       (let [new-scope (.newObject ctx parent)]
         (.setParentScope new-scope parent)
         new-scope))))