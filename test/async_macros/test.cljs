(ns async-macros.test
  (:require-macros [cljs.test :refer [run-tests]]))

(enable-console-print!)

(defn ^:export run [] (println "Fertig!"))
