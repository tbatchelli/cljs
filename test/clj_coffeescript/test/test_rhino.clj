(ns clj-coffeescript.test.test-rhino
  (:use clj-coffeescript.rhino :reload)
  (:use clojure.test))

(deftest test-scopes
  (testing "building scopes"
    (is (= (let [root-scope (build-scope)]
             ;; create and test a property to make sure that the scope works
             (.put root-scope "test-var" root-scope "check-value")
             (.get root-scope "test-var" root-scope))
           "check-value"))))

