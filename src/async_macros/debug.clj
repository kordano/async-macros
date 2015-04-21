(ns async-macros.debug
  (:require [clojure.core.async :as async
             :refer [<!! <! >! timeout chan alt! go go-loop]]))

(defn cljs?
  []
  (if-let [cljs-ns-var (resolve 'cljs.analyzer/*cljs-ns*)]
    (some? @cljs-ns-var)
    false))

(defn throwable? [x]
  (instance? Throwable x))

(defn throw-err [e]
  (when (throwable? e) (throw e)) e)

(defmacro <? [ch]
  `(throw-err (<! ~ch)))

(defmacro <!? [ch]
  `(throw-err (if cljs?
                (go (<! ~ch))
                (<!! ~ch))))

(defmacro go<? [& body]
  `(go (try
         ~@body
         (catch Exception e#
           e#))))

(defmacro go>? [err-chan & body]
  `(go (try
         ~@body
         (catch Exception e#
           (>! ~err-chan e#)))))

(defmacro go-loop>? [err-chan bindings & body]
  `(go (try
         (loop ~bindings
           ~@body)
         (catch Exception e#
           (>! ~err-chan e#)))))

(defmacro go-loop<? [bindings & body]
  `(go<? (loop ~bindings ~@body) ))
