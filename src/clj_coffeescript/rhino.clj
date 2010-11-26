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

(defn load-stream
  ([scope file-name stream]
     (with-context [ctx]
       (load-stream scope file-name stream ctx)))
  ([scope file-name stream ctx]
     (.evaluateReader ctx scope stream file-name 0 nil)
     (println file-name " loaded.")))