(ns cev.engine.noise
  (:import FastNoiseLite FastNoiseLite$NoiseType))

(defn noise-2d [width height scale]
  (let [noise (FastNoiseLite.)]
    (.SetNoiseType noise FastNoiseLite$NoiseType/Perlin)
    (.SetSeed noise (int (* (Math/random) Integer/MAX_VALUE)))
    (into
     []
     (for [x (range width)
           y (range height)]
       (-> (.GetNoise noise (* x scale) (* y scale))
           (+ 1) (/ 2))))))
