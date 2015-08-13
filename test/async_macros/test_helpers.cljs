(ns async-macros.test-helpers)

(defn debug [x]
  (.log js/console x)
  x)

(defn latch [m f]
  (let [r (atom 0)]
    (add-watch r :latch
      (fn [_ _ o n]
        (when (== n m) (f))))
    r))

(defn inc! [r]
  (swap! r inc))

