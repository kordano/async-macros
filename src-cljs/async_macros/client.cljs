(ns async-macros.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [async-macros.core :refer [<<! <<? <? go-try go-try> go-loop-try go-loop-try> go-for]])
  (:require [async-macros.core :refer [throwable?]]
            [cljs.core.async :refer [close! chan <! >! alts! into chan]]))

#_(cemerick.austin.repls/cljs-repl (cemerick.austin/exec-env))

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

(comment

  (require 'async-macros.client)
  
  (in-ns 'async-macros.client)

  (def err-chan (chan))

  (go
    (println
     (try
       (do
         (go-loop-try>
          err-chan
          [ch (chan 2)
           inputs ["1" (js/Error.)]]
          (do
            (>! ch (first inputs))
            (recur ch (rest inputs))))
         (<? err-chan))
       (catch js/Error e :fail))))


  (go
    (println
     (<?
      (go-loop-try
       [ch (chan 2)
        inputs ["1" "2"]]
       (when-not (empty? inputs)
         (>! ch (first inputs))
         (throw (js/Error. "fail"))
         (recur ch (rest inputs)))))))


  
  (go
    (println
     (try
       (do
         (go-try> err-chan (let [ch (chan 2)]
                             (>! ch "1")
                             (>! ch (js/Error.))
                             (close! ch)
                             (<<? ch)))
         (<? err-chan))
       (catch js/Error e :fail)))) 
  
  )
