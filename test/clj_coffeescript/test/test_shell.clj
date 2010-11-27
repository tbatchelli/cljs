(ns clj-coffeescript.test.test-shell
  (:use clj-coffeescript.shell :reload)
  (:use clojure.test))

(deftest test-with-new-scope
  (is (= 12
         (with-new-scope
           (cs "a=12;")))
      (= 12
         (with-new-scope
           (js "var a;a=12;")))))

(deftest test-with-existing-scope
  (is (= 12
         (let [scope (new-scope)]
           (with-scope [scope]
             (cs "a=12;"))))
      (= 12
         (let [scope (new-scope)]
           (with-scope [scope]
             (js "var a;a=12;"))))))

(deftest test-cs-direct
  (is (= 12
         (let [scope (new-scope)]
           (cs "a=12" scope)))))

(deftest test-js-direct
  (is (= 12
         (let [scope (new-scope)]
           (js "var a;a=12;" scope)))))