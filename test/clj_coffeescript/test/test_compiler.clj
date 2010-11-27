(ns clj-coffeescript.test.test-compiler
  (:use clj-coffeescript.compiler :reload)
  (:require [clj-coffeescript.rhino :as rhino])
  (:use clojure.test))

(deftest end-to-end
  (testing "Building the compiler and compiling one line"
    (is (= (let [compiler (build-compiler)]
             (compile-string compiler "a=12" true)))
        "var a;\na = 12;"))
  (testing "compiling and running coffeescript code"
    (is (= (let [compiler (build-compiler)
                 runtime (rhino/build-scope)]
             (evaluate-string compiler runtime "a=1+3;")))
        4)))