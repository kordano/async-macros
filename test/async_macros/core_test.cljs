(ns async-macros.core-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [async-macros.core :refer [throwable?] :refer-macros [<? go-try <<? <<! alt?]]
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

(run-tests)

