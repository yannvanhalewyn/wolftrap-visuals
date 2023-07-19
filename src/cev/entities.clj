(ns cev.entities
  (:require
   [cev.gl.entity :as entity]
   [cev.gl.shader :as shader]))

(def fractal-canvas
  (entity/make
   {:entity/id :fractal-canvas
    :entity/name "Fractal Canvas"

    :mesh/vertices
    [-1.0 -1.0
     -1.0  1.0
     1.0  1.0
     1.0 -1.0]
    :mesh/indices
    [0 1 2 0 2 3]

    :glsl/vertex-source (shader/resource-file "canvas.vert")
    :glsl/fragment-source (shader/resource-file "distance_fractal.frag")
    :glsl/attributes
    [{:glsl/name "pos" :glsl/dimensions 2}]}))

(def rgb-triangle
  (entity/make
   {:entity/name "RGB Triangle"
    ;; :entity/enabled? true

    :mesh/vertices
    [-1.0 -1.0 0.0 1.0 0.0 0.0 0.6
     0.0  1.0 0.0 0.0 1.0 0.0 0.0
     1.0 -1.0 0.0 0.0 0.0 1.0 1.0]
    :mesh/indices
    [0 1 2]

    :glsl/vertex-source (shader/resource-file "playground.vert")
    :glsl/fragment-source (shader/resource-file "playground.frag")
    :glsl/attributes
    [{:glsl/name "vpos" :glsl/dimensions 3}
     {:glsl/name "vcol" :glsl/dimensions 3}
     {:glsl/name "vopacity" :glsl/dimensions 1}]}))

(def texture
  (entity/make
   {:entity/name "Texture"
    :entity/enabled? true

    :mesh/vertices
    [ 1.0  1.0 0.0 1.0 1.0
     -1.0  1.0 0.0 0.0 1.0
     -1.0 -1.0 0.0 0.0 0.0
     1.0 -1.0 0.0 1.0 0.0]

    :mesh/indices
    [0 1 2
     2 3 0]

    :mesh/texture
    {:texture/pixels
     [0.0 0.0 1.0
      0.0 1.0 0.0
      1.0 0.0 0.0
      1.0 1.0 1.0]
     :glsl/name "tex"}

    :glsl/vertex-source (shader/resource-file "texture.vert")
    :glsl/fragment-source (shader/resource-file "texture.frag")

    :glsl/attributes
    [{:glsl/name "point" :glsl/dimensions 3}
     {:glsl/name "texcoord" :glsl/dimensions 2}]}))

(defn enabled-entities []
  (filter :entity/enabled? [fractal-canvas rgb-triangle texture]))
