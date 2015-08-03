(ns async-macros.core-test
  (:require [cljs.core.async :refer [chan]])
  (:require-macros [async-macros.core :refer [<! throwable?]]
                   [cljs.test :refer [deftest is]]))


(deftest test-throwable
  (testing "Javascript Exception throwables."
    (is (= (am/throwable? js/Object) true))))
