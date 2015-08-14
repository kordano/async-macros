(ns async-macros.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [async-macros.core :refer [<<! <<? <? go-try got-try> ]])
  (:require [async-macros.core :refer [throwable?]]
            [cljs.core.async :refer [close! chan <! >! alts! into chan]]))

(enable-console-print!)

(defn run-it []
  (go
    (println
     (<! (go
           (<<! (let [ch (chan 2)]
                  (>! ch "1")
                  (>! ch "2")
                  (close! ch)
                  ch)))))))
