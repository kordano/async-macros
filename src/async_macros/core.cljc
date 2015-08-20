(ns async-macros.core
  #?(:cljs (:require [cljs.core.async :refer [<! >! alts! into chan] :as async]))
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])))

#?(:cljs
    (defn throwable?
      "Check if it is throwable."
      [x]
      (instance? js/Error x)))

#?(:cljs
   (defn throw-if-throwable
     "Helper method that checks if x is js/Error and if yes, wraps it in a new
  exception, passing though ex-data if any, and throws it. The wrapping is done
  to maintain a full stack trace when jumping between multiple contexts."
     [x]
     (when (throwable? x) (throw x)) x))

(defmacro <?
  "Same as core.async <! but throws an exception if the channel returns a
  throwable object. Also will not crash if channel is nil."
  [ch]
  `(throw-if-throwable (let [ch# ~ch] (when ch# (cljs.core.async/<! ch#)))))

(defmacro <<!
  "Takes multiple results from a channel and returns them as a vector.
  The input channel must be closed."
  [ch]
  `(let [ch# ~ch]
     (cljs.core.async/<! (cljs.core.async/into [] ch#))))

(defmacro <<?
  "Takes multiple results from a channel and returns them as a vector.
  Throws if any result is an exception."
  [ch]
  `(->> (<<! ~ch)
        (map throw-if-throwable)
        ; doall to check for throwables right away
        (doall)))

(defmacro alt?
  "Same as core.async alt! but throws an exception if the channel returns a
  throwable object."
  [& clauses]
  `(throw-if-throwable (cljs.core.async.macros/alt! ~@clauses)))

(defmacro alts?
  "Same as core.async alts! but throws an exception if the channel returns a
  throwable object."
  [ports]
  `(let [[val# port#] (alts! ~ports)]
     [(throw-if-throwable val#) port#]))

(defmacro go-try
  "Asynchronously executes the body in a go block. Returns a channel which
  will receive the result of the body when completed or an exception if one
  is thrown."
  [& body]
  `(cljs.core.async.macros/go (try ~@body (catch js/Error e# e#))))

(defmacro go-try>
  "Same as go-try, but puts errors directly on a channel and returns
  nil on the resulting channel."
  [err-chan & body]
  `(cljs.core.async.macros/go
     (try
       ~@body
       (catch js/Error e#
         (cljs.core.async/>! ~err-chan e#)))))

(defmacro go-loop-try
  "Returns result of the loop or a throwable in case of an exception."
  [bindings & body]
  `(go-try (loop ~bindings ~@body) ))

(defmacro go-loop-try>
  "Put throwables arising in the go-loop on an error channel."
  [err-chan bindings & body]
  `(cljs.core.async.macros/go
     (try
       (loop ~bindings
         ~@body)
       (catch js/Error e#
         (cljs.core.async/>! ~err-chan e#)))))

(defmacro ^{:private true} assert-args
   [& pairs]
   `(do (when-not ~(first pairs)
          (throw (IllegalArgumentException.
                  (str (first ~'&form) " requires " ~(second pairs) " in " ~'*ns* ":" (:line (meta ~'&form))))))
        ~(let [more (nnext pairs)]
           (when more
             (list* `assert-args more)))))

(defmacro go-for
   "List comprehension adapted from clojure.core 1.7. Takes a vector of
  one or more binding-form/collection-expr pairs, each followed by
  zero or more modifiers, and yields a channel of evaluations of
  expr. It is eager on all but the outer-most collection. TODO

  Collections are iterated in a nested fashion, rightmost fastest, and
  nested coll-exprs can refer to bindings created in prior
  binding-forms.  Supported modifiers are: :let [binding-form expr
  ...],
   :while test, :when test. If a top-level entry is nil, it is skipped
  as it cannot be put on channel.

  (<! (cljs.core.async/into [] (go-for [x (range 10) :let [y (<! (go 4))] :while (< x y)] [x y])))"
   {:added "1.0"}
   [seq-exprs body-expr]
   (assert-args
    (vector? seq-exprs) "a vector for its binding"
    (even? (count seq-exprs)) "an even number of forms in binding vector")
   (let [to-groups (fn [seq-exprs]
                     (reduce (fn [groups [k v]]
                               (if (keyword? k)
                                 (conj (pop groups) (conj (peek groups) [k v]))
                                 (conj groups [k v])))
                             [] (partition 2 seq-exprs)))
         err (fn [& msg] (throw (IllegalArgumentException. ^String (apply str msg))))
         emit-bind (fn emit-bind [res-ch [[bind expr & mod-pairs]
                                          & [[_ next-expr] :as next-groups]]]
                     (let [giter (gensym "iter__")
                           gxs (gensym "s__")
                           do-mod (fn do-mod [[[k v :as pair] & etc]]
                                    (cond
                                      (= k :let) `(let ~v ~(do-mod etc))
                                      (= k :while) `(when ~v ~(do-mod etc))
                                      (= k :when) `(if ~v
                                                     ~(do-mod etc)
                                                     (recur (rest ~gxs)))
                                      (keyword? k) (err "Invalid 'for' keyword " k)
                                      next-groups
                                      `(let [iterys# ~(emit-bind res-ch next-groups)
                                             fs# (<? (iterys# ~next-expr))]
                                         (if fs#
                                           (concat fs# (<? (~giter (rest ~gxs))))
                                           (recur (rest ~gxs))))
                                      :else `(let [res# ~body-expr]
                                               (when res# (cljs.core.async/>! ~res-ch res#))
                                               (<? (~giter (rest ~gxs))))
                                      #_`(cons ~body-expr (<? (~giter (rest ~gxs))))))]
                       `(fn ~giter [~gxs]
                          (go-try
                           (loop [~gxs ~gxs]
                             (let [~gxs (seq ~gxs)]
                               (when-let [~bind (first ~gxs)]
                                 ~(do-mod mod-pairs))))))))
         res-ch (gensym "res_ch__")]
     `(let [~res-ch (cljs.core.async/chan)
            iter# ~(emit-bind res-ch (to-groups seq-exprs))]
        (cljs.core.async.macros/go (try (<? (iter# ~(second seq-exprs)))
                 (catch js/Error e#
                   (cljs.core.async/>! ~res-ch e#))
                 (finally (cljs.core.async/close! ~res-ch))))
        ~res-ch)))



(comment
(defmacro <<!
  "Takes multiple results from a channel and returns them as a vector.
  The input channel must be closed."
  [ch]
  `(let [ch# ~ch]
     (<! (cljs.core.async/into [] ch#))))

(defmacro <!*
  "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels."
  [chs]
  `(let [chs# ~chs]
     (loop [chs# chs#
            results# (cljs.core.PersistentQueue/EMPTY)]
       (if-let [head# (first chs#)]
         (->> (<! head#)
              (conj results#)
              (recur (rest chs#)))
         (vec results#)))))

#?(:cljs
   (defmacro <?*
     "Takes one result from each channel and returns them as a collection.
  The results maintain the order of channels. Throws if any of the
  channels returns an exception."
     [chs]
     `(let [chs# ~chs]
        (loop [chs# chs#
               results# (cljs.core.PersistentQueue/EMPTY)]
          (if-let [head# (first chs#)]
            (->> (<? head#)
                 (conj results#)
                 (recur (rest chs#)))
            (vec results#))))))



#?(:cljs
   (defn pmap>>
     "Takes objects from ch, asynchrously applies function f> (function should
  return channel), takes the result from the returned channel and if it's not
  nil, puts it in the results channel. Returns the results channel. Closes the
  returned channel when the input channel has been completely consumed and all
  objects have been processed."
     [f> parallelism ch]
     {:pre [(fn? f>)
            (and (integer? parallelism) (pos? parallelism))
            (instance? ReadPort ch)]}
     (let [results (async/chan)
           threads (atom parallelism)]
       (dotimes [_ parallelism]
         (go
           (loop []
             (when-let [obj (<! ch)]
               (if (instance? js/Error obj)
                 (do
                   (>! results obj)
                   (async/close! results))
                 (do
                   (when-let [result (<! (f> obj))]
                     (>! results result))
                   (recur)))))
           (when (zero? (swap! threads dec))
             (async/close! results))))
       results)))

(defn engulf
  "Similiar to dorun. Simply takes messages from channel but does nothing with
  them. Returns channel that will close when all messages have been consumed."
  [ch]
  (go-loop []
    (when (<! ch) (recur))))

#?(:cljs
   (defn reduce>
     "Performs a reduce on objects from ch with the function f> (which should return
  a channel). Returns a channel with the resulting value."
     [f> acc ch]
     (let [result (chan)]
       (go-loop [acc acc]
         (if-let [x (<! ch)]
           (if (instance? js/Error x)
             (do
               (>! result x)
               (async/close! result))
             (->> (f> acc x) <! recur))
           (do
             (>! result acc)
             (async/close! result))))
       result)))



)
