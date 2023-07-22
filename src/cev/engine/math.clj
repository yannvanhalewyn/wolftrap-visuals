(ns cev.engine.math)

(defn random
  ([] (Math/random))
  ([range] (* (Math/random) range))
  ([min max] (-> (Math/random)
                 (* (- max min))
                 (+ min))))

(definterface IVec2
  (getX [])
  (setX [v])
  (getY [])
  (setY [v]))

(deftype Vec2 [^:unsynchronized-mutable x
               ^:unsynchronized-mutable y]
  IVec2
  (getX [_] x)
  (setX [_this v] (set! x v))
  (getY [_] y)
  (setY [_this v] (set! y v)))

(defn vec2 [x y]
  (Vec2. x y))

(defn ->clj [v]
  [(.getX v) (.getY v)])

(defn x [v] (.getX v))
(defn y [v] (.getY v))
