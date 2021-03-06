(ns async-macros.core-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [async-macros.core :refer [throwable?] :refer-macros [<? <<? <<! go-try go-try> alt? go-loop-try go-loop-try> go-for]]
            [async-macros.test-helpers :refer [latch inc! debug]]
            [cljs.core.async :refer [chan close! take! >! into]]
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

(deftest javascript-throwables
  (testing "test if Javascrip Error is correctly throwable"
    (is (= (throwable? (js/Error.)) true))
    (is (= (throwable? (js/Object.)) false))))


(deftest identity-chan-is-not-buggy
  (async done
    (go
      (is (= (<! (identity-chan 42)) 42))
      (done))))


(deftest take-from-channel-with-throwable
  (async done
         (go
           (is (= (<? (identity-chan 42)) 42))
           (done))))


(deftest take-multi-from-channel
  (async done
         (go
           (is (= ["1" "2"]
                  (<!
                   (go
                     (<<!
                      (let [ch (chan 2)]
                        (>! ch "1")
                        (>! ch "2")
                        (close! ch)
                        ch))))))
           (done))))


(deftest take-multi-from-channel-with-exception
  (async done
         (go
           (is (= ["1" "2"]
                  (<! (go (<<? (let [ch (chan 2)]
                                 (>! ch "1")
                                 (>! ch "2")
                                 (close! ch)
                                 ch))))))
           (done))))


(deftest alt-with-throwable
  (async done
         (let [c (identity-chan 42)]
           (go
             (is (= [42 :foo]
                    (alt? (identity-chan 42)
                          ([c] [c :foo]))))
             (done)))))


(deftest go-with-throwable
  (async done
         (go
           (is (thrown? js/Error
                        (<? (go-try (let [ch (chan 2)]
                                      (>! ch "1")
                                      (>! ch (js/Error.))
                                      (close! ch)
                                      (<<? ch))))))
           (done))))


(deftest go-with-throwable-and-error-chan
  (let [err-chan (chan)]
    (async done
           (go
             (is (thrown? js/Error
                          (do
                            (go-try> err-chan (let [ch (chan 2)]
                                                (>! ch "1")
                                                (>! ch (js/Error.))
                                                (close! ch)
                                                (<<? ch)))
                            (<? err-chan))))
             (done)))))

(deftest go-loop-with-throwable
  (async done
         (go
           (is (thrown? js/Error
                        (<?
                         (go-loop-try
                          [ch (chan 2)
                           inputs ["1" "2"]]
                          (when-not (empty? inputs)
                            (>! ch (first inputs))
                            (throw (js/Error.))
                            (recur ch (rest inputs)))))))
           (done))))


(deftest go-loop-with-throwable-and-chan
  (let [err-chan (chan)]
    (async done
           (go
             (is (thrown? js/Error
                          (do
                            (go-loop-try>
                             err-chan
                             [c0 (chan 2)
                              inputs ["1" "2"]]
                             (when-not (empty? inputs)
                               (>! c0 (first inputs))
                               (throw (js/Error.))
                               (recur c0 (rest inputs))))
                            (<? err-chan))))
             (done)))))


(deftest go-list-comprehension
  (async done
         (go
           (is (= [[0 4] [1 4] [2 4] [3 4]]
                  (<!
                   (into []
                         (go-for [x (range 10)
                                  :let [y (<! (go 4))]
                                  :while (< x y)]
                                 [x y])))))
           (done))))


(run-tests)
