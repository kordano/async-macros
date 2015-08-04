(ns async-macros.core-test
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [async-macros.core :refer [throwable?] :refer-macros [<?]]
            [cljs.core.async :refer [chan close! take! >!]]
            [cljs.test :refer-macros [deftest is testing async run-tests]]))

(println "first")

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(deftest test-throwable
  (is (= (throwable? (js/Error.)) true))
  (is (= (throwable? (js/Object.)) false)))

(deftest example-with-timeout
  (async done
         (take! (go (println (<! (go 42))))
                (fn []
                  ;; make assertions in async context...
                  (done) ;; ...then call done
                  ))))

(run-tests)

(println "last")

