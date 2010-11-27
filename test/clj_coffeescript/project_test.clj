(ns clj-coffeescript.project-test
  (:use
   clojure.test
   clj-coffeescript.project))


(def p1 (project
         :coffee-path "test-resources/src/coffee"
         :coffee-test-path "test-resources/test/coffee"))

(deftest test-all-test
  (test-all p1))
