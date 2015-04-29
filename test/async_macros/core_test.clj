(ns async-macros.core-test
  (:require [clojure.test :refer :all]
            [async-macros.core :refer :all]))

(deftest test-throwable
  (testing "Java Exception throwables."
    (is (= (throwable? (java.lang.Exception.)) true))))
