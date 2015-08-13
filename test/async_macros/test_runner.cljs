(ns async-macros.test-runner
  (:require-macros [cljs.test :refer [run-tests]]) 
  (:require [async-macros.core-test]))

(enable-console-print!)

(defn ^:export run []
  (run-tests ['async-macros.core-test])
  (println "All tests finished!"))
