(ns cljs.test.test-jquery
  (:require [cljs.rhino :as rhino] :reload)
  (:use cljs.compiler :reload)
  (:use clojure.test))

(def *compiler*)
(def *runtime*)
(defn compiler-fixture [f]
  (binding [*compiler* (build-compiler)]
    (f)))
(defn rutime-fixture [f]
  (binding [*runtime* (rhino/build-scope)]
    (f)))

(use-fixtures :once compiler-fixture)
(use-fixtures :each rutime-fixture)

(deftest compile
  (is (= (compile-string *compiler* "a=12;" true)
         "var a;\na = 12;")))

(deftest evaluation-scope-continuity
  (is (= (evaluate-string *compiler* *runtime* "a=12;")
         12) "A variable is defined in the runtime scope")
  (is (= (evaluate-string *compiler* *runtime* "a;")
         12) "The variable is still in the scope after a second test"))

(deftest evaluation-scope-discontinuity
  (is (thrown? org.mozilla.javascript.EcmaError (evaluate-string *compiler* *runtime* "a;")
         12) "The variable didn't survive in the scope between tests."))
