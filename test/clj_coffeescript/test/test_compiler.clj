(ns clj-coffeescript.test.test-compiler
  (:use clj-coffeescript.compiler :reload)
  (:require [clj-coffeescript.rhino :as rhino])
  (:use clojure.test))

(deftest end-to-end
  (testing "Building the compiler and compiling one line"
    (is (= (let [compiler (build-compiler)]
             (compile-string compiler "a=12" true))
           "var a;\na = 12;")))
  (testing "compiling and running coffeescript code"
    (is (= (let [compiler (build-compiler)
                 runtime (rhino/build-scope)]
             (evaluate-string compiler runtime "a=1+3;"))
           4)))
  (testing "evaluating scripts in scopes work"
    (is (= (let [compiler (build-compiler)
                 runtime-1 (rhino/build-scope)
                 runtime-2 (rhino/build-scope)]
             (evaluate-string compiler runtime-1 "a=1;")
             (evaluate-string compiler runtime-2 "a=2;")
             (evaluate-string compiler runtime-1 "b=a*a;"))
           1))))