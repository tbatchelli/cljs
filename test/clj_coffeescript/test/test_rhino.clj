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
    (is (= (let [root-scope (build-scope)]
             (with-context [ctx]
               (set-context-interpreted ctx)
               ;; hack to make env.js load properly -- rhino doesn't
               ;; define this non-ECMA function. Neither does env.js 
               (evaluate-string "function print(message) {java.lang.System.out.println(message);}"
                                "prevent 'print' error"
                                root-scope
                                ctx)
               (load-resources ["resources/env.rhino.1.2.js" "resources/jquery.js" ] root-scope ctx)
               (evaluate-string "Envjs({ scriptTypes : { '': true,  'text/javascript': true, 'text/envjs': false }});" "execute javascript" root-scope ctx)
               (println "about to load page")
               (evaluate-string "window.location='http://www.envjs.com';" "env test" root-scope ctx)
               (println "page loaded")
               (evaluate-string "$('#welcome > p').text()" "jquery test" root-scope ctx)))
           "              Envjs is a simulated browser environment written              in javascript.  It was originally developed by               John Resig              and discussed in his blog               here.               Envjs is now supported by a community of              developers who all use Envjs as part of their own              open source projects.           "))))