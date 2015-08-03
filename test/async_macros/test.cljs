(ns async-macros.test
  (:require-macros [cljs.test :refer [run-all-tests]]))

(enable-console-print!)

(defn ^:export run
  []
  (run-all-tests #"async-macros.*-test"))
