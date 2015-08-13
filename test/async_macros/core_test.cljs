(ns async-macros.core-test
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [async-macros.core :refer [throwable?] :refer-macros [<? go-try <<? <<!]]
            [async-macros.test-helpers :refer [latch inc! debug]]
            [cljs.core.async :refer [chan close! take! >!]]
            [cljs.test :refer-macros [deftest is testing async run-tests]]))


(enable-console-print!)


(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))


(defn identity-chan
  [x]
  (let [c (chan 1)]
    (go (>! c x)
        (close! c))
    c))


(deftest test-throwable
  (testing "test if Javascrip Error is correctly throwable"
    (is (= (throwable? (js/Error.)) true))
    (is (= (throwable? (js/Object.)) false))))


(deftest test-identity-chan
  (async done
    (go
      (is (= (<! (identity-chan 42)) 42))
      (done))))


(deftest alt-tests
  (async done
    (testing "alts! works at all"
      (let [c (identity-chan 42)]
        (go
          (is (= [42 c] (alts! [c])))
          (done))))))


(deftest test-<?
  (async done
         (go
           (is (= (<? (identity-chan 42)) 42))
           (done))))


#_(deftest test-go-try
    (async
     done
     (go
       (is (thrown? js/Error
                    (go-try (let [ch (identity-chan 2)]
                              (>! ch "1")
                              (>! ch (js/Error.))
                              (close! ch)
                              (<<? ch)))))
       (done))))


(deftest test-<!!
  (async done
         (go
           (is (= ["1" "2"]
                  (<! (go (<<! (let [ch (chan 2)]
                                 (>! ch "1")
                                 (>! ch "2")
                                 (close! ch)
                                 ch))))))
           (done))))


(run-tests)
