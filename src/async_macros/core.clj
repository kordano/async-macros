(ns async-macros.core)

(defn cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs.
  https://github.com/Prismatic/schema/blob/master/src/clj/schema/macros.clj"
  [env]
  (boolean (:ns env)))

(defmacro if-cljs
  "Return then if we are generating cljs code and else for Clojure code.
   https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
  [then else]
  (if (cljs-env? &env) then else))

(if-cljs
 (do
   (require '[cljs.core.async :refer [<! >!]])
   (require-macros '[cljs.core.async.macros :refer [go]]))
 (require '[clojure.core.async :refer [<! >! <!! go]]))

(defn throwable?
  "Check if it is throwable."
  [x]
  (instance? (if-cljs
              js/Error.prototype
              java.lang.Throwable) x))

(defn throw-err
  "Throw the damn thing."
  [e]
  (when (throwable? e) (throw e)) e)

(defmacro <? [ch]
  `(throw-err (<! ~ch)))

(if-cljs
 nil
  (defmacro <!? [ch]
    `(throw-err (<!! ~ch))))

(defmacro go<? [& body]
  `(go (try
         ~@body
         (catch (if-cljs js/Error java.lang.Exception) e#
           e#))))

(defmacro go>? [err-chan & body]
  `(go (try
         ~@body
         (catch (if-cljs js/Error java.lang.Exception) e#
           (>! ~err-chan e#)))))

(defmacro go-loop>? [err-chan bindings & body]
  `(go (try
         (loop ~bindings
           ~@body)
         (catch (if-cljs? js/Error java.lang.Exception) e#
           (>! ~err-chan e#)))))

(defmacro go-loop<? [bindings & body]
  `(go<? (loop ~bindings ~@body) ))
