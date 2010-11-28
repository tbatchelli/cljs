(ns clj-coffeescript.rhino
  (:import [org.mozilla.javascript Context ContextFactory]
           [org.mozilla.javascript.tools.shell Global Main]
           [java.io InputStreamReader]))

(defmacro with-context [[ctx] & body]
  `(try (let [~ctx (Context/enter)]
          ~@body)
        (finally (Context/exit))))

(defn set-context-interpreted [ctx]
  (.setOptimizationLevel ctx -1))

(defn get-resource [uri]
  (.getResourceAsStream (clojure.lang.RT/baseLoader) uri))

(defn get-resource-as-stream [uri]
  (let [resource (get-resource uri)]
    (InputStreamReader. resource "UTF-8")))

(defn build-global-scope []
  (let [global-scope (Global.)
        ctx (.enterContext (ContextFactory/getGlobal))]
    (try
      (.init global-scope ctx)
      (set-context-interpreted ctx)
      global-scope
      (finally (Context/exit)))))

(defonce *global-scope* (build-global-scope))

(defn build-shell-scope []
  (with-context [ctx]
    (.initStandardObjects ctx *global-scope*)))

(defn build-scope
  ([]
     (with-context [ctx]
       (.initStandardObjects ctx)))
  ([parent]
     (with-context [ctx]
       (let [new-scope (.newObject ctx parent)]
         (.setParentScope new-scope parent)
         new-scope))))

(defn load-stream
  ([stream file-name scope]
     (with-context [ctx]
       (load-stream stream file-name scope ctx)))
  ([stream file-name scope ctx]
     (.evaluateReader ctx scope stream file-name 0 nil)
     (println file-name " loaded.")))

(defn load-resources [resources scope ctx]
  (let [load-resource (fn [uri]
                        (with-open [stream (get-resource-as-stream uri)]
                          (load-stream stream uri scope ctx )))]
    (doall (map load-resource resources))))


(defn set-named-property
  ([name value target start]
     (with-context [ctx]
       (set-named-property name value target start ctx)))
  ([name value target start ctx]
     (.put target name start value)))

(defn evaluate-string
  ([string name scope]
     (with-context [ctx]
       (evaluate-string string name scope ctx)))
  ([string name scope ctx]
     (.evaluateString ctx scope string name 0 nil)))

;;; env.js stuff

(defn envjs-turn-on-javascript [scope ctx]
  (evaluate-string "Envjs({ scriptTypes : { '': true,  'text/javascript': true, 'text/envjs': true }});" "execute javascript" scope ctx))

(comment "maybe get the line number of the caller to report errors"
         (defmacro f [] (println (pr-str (meta &form))))
         (f))