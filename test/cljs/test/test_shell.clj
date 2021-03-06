(ns cljs.test.test-shell
  (:use cljs.shell :reload-all)
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

(deftest test-new-scope
  (let [parent (new-scope)]
    (is (new-scope parent))))

(deftest test-set-scope
  (let [original-scope *runtime*
        new-scope (new-scope)]
    (try
      (alter-var-root #'*runtime* (fn [_] nil))
      (set-scope new-scope)
      (is (= *runtime* new-scope) "The runtime has been set")
      (finally
       (alter-var-root #'*runtime* (fn [_] original-scope))))
    (is (= *runtime* original-scope) "Making sure this test didn't pollute")))

(deftest test-load-envjs
  (with-new-scope
    (is (load-library "envjs") "loading envjs first creates the 'print' function for it to load properly")))


(deftest test-load-jquery
  (with-new-scope
    (is (load-library "jquery") "loading jquery should also load envjs")))

;; todo -- this test is too fragile. Think of something else...
#_(deftest test-load-page-jquery
  (is (= (with-new-scope
           (load-library "jquery")
           (js "window.location='http://palletops.com'; $('a').text();"))
         "PalletBlogAboutSource on GitHubAPI docspalletconfigurationcredentialshowtoreleaseSubscribe to the Pallet Blog »Unsubscribe »Subscribe via RSSAboutPalletBuild you environments in the cloud with palletSo Clojure walks into a cloudContinuous Deployment of Clojure Web Applications")))

