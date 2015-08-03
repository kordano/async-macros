(ns async-macros.core-test
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [async-macros.core :refer [throwable?] :refer-macros [go-try]]
            [cljs.core.async :refer [chan close! >!]]
            [cljs.test :refer-macros [deftest is testing]]))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(deftest test-throwable
  (is (= (throwable? (js/Error.)) true))
  (is (= (throwable? (js/Object.)) false)))


(deftest test-go-try
  (is
   (= (<? (go (<? (go-try (let [ch (chan 2)]
                            (>! ch "1")
                            (>! ch (js/Error.))
                            (close! ch)
                            (<<? ch))))))
      (throws js/Error))))
