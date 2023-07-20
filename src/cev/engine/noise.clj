(ns cev.engine.noise
  (:import FastNoiseLite
           [FastNoiseLite NoiseType]))

(defn noise2 [width height]
  (let [noise (FastNoiseLite.)]
    ;; (.SetNoiseType noise FastNoiseLite/NoiseType.OpenSimplex2)
    (into
     []
     (for [x (range width)
           y (range height)]
       (.GetNoise noise x y)))))
