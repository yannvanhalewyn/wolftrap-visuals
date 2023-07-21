(ns cev.engine.math)

(defn random
  ([] (Math/random))
  ([range] (* (Math/random) range))
  ([min max] (-> (Math/random)
                 (* (- max min))
                 (+ min))))

(def x first)
(def y second)
