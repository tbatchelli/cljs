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

(deftest libraries
  (testing "loading and using underscore.js"
    (is (= (let [root-scope (build-scope)]
             (with-context [ctx]
               (load-resources ["resources/underscore.js"] root-scope ctx)
               (evaluate-string "_([1, 2, 3]).reduce(function(a,b){ return a+b; });" "underscore test" root-scope)))
           6)))
  (testing "loading env.rhino.js, jquery.js, load a page and see if jquery can parse it"
    (is (= (let [root-scope (build-shell-scope)]
             (with-context [ctx]
               (set-context-interpreted ctx)
               (load-resources ["resources/env.rhino.1.2.js" "resources/jquery.js" ] root-scope ctx)
               (envjs-turn-on-javascript root-scope ctx)
               (println "about to load page")
               (evaluate-string "window.location='test/test-resources/simple.html';" "envjs test" root-scope ctx)
               (println "page loaded")
               (evaluate-string "$('#mydiv > p').text()" "jquery test" root-scope ctx)))
           "hello world!")))
  (testing "env.rhino.js, jquery.js and qunit.js"
    (is (= (let [scope (build-shell-scope)]
             (with-context [ctx]
               (set-context-interpreted ctx)
               (load-resources ["resources/env.rhino.1.2.js"
                                "resources/jquery.js" ] scope ctx)
               (envjs-turn-on-javascript scope ctx)
               (evaluate-string "window.location='test/test-resources/qunit-test.html';" "qunit+envjs test" scope ctx)))
           "test/test-resources/qunit-test.html"))))