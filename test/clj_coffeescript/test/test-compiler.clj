(ns clj-coffeescript.test.test-compiler
  (:use clj-coffeescript.compiler :reload)
  (:use clojure.test))

(deftest end-to-end
  (testing "Building the compiler and compiling one line"
    (is (= (let [compiler (build-compiler)]
             (compile-string compiler "a=12" true)))
        "var a;\na = 12;")))