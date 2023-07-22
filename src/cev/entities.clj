(ns cev.entities
  (:require
   [cev.engine.entity :as entity]
   [cev.engine.shader :as shader]
   [cev.engine.noise :as noise]))

(def fractal-canvas
  (entity/make
   {:entity/name "Fractal Canvas"
    :entity/enabled? true

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
    :entity/enabled? false

    :mesh/vertices
    [-1.0 -1.0 0.0 1.0 0.0 0.0
      0.0  1.0 0.0 0.0 1.0 0.0
      1.0 -1.0 0.0 0.0 0.0 1.0]
    :mesh/indices
    [0 1 2]

    :glsl/vertex-source (shader/resource-file "triangle.vert")
    :glsl/fragment-source (shader/resource-file "triangle.frag")
    :glsl/attributes
    [{:glsl/name "vpos" :glsl/dimensions 3}
     {:glsl/name "vcol" :glsl/dimensions 3}]}))

(def texture
  (entity/make
   {:entity/name "Texture"
    :entity/enabled? false

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
     [0.0 0.0 1.0  ;; Blue
      0.0 1.0 0.0  ;; Green
      1.0 0.0 0.0  ;; Red
      1.0 1.0 1.0] ;; White
     :texture/format :gl/rgb
     :texture/width 2
     :texture/height 2
     :glsl/name "tex"}

    :glsl/vertex-source (shader/resource-file "texture.vert")
    :glsl/fragment-source (shader/resource-file "texture3.frag")

    :glsl/attributes
    [{:glsl/name "screenUV" :glsl/dimensions 3}
     {:glsl/name "textureUV" :glsl/dimensions 2}]}))

(defn make-noise []
  (entity/make
   {:entity/name "Noise"
    ;; :entity/enabled? true

    :mesh/vertices
    [ 1.0  1.0 0.0 1.0 1.0
     -1.0  1.0 0.0 0.0 1.0
     -1.0 -1.0 0.0 0.0 0.0
      1.0 -1.0 0.0 1.0 0.0]

    :mesh/indices
    [0 1 2
     2 3 0]

    :mesh/texture
    {:texture/pixels (noise/noise-2d 128 128 2)
     :texture/format :gl/depth-component
     :texture/width 128
     :texture/height 128
     :glsl/name "tex"}

    :glsl/vertex-source (shader/resource-file "texture.vert")
    :glsl/fragment-source (shader/resource-file "texture1.frag")

    :glsl/attributes
    [{:glsl/name "point" :glsl/dimensions 3}
     {:glsl/name "texcoord" :glsl/dimensions 2}]}))

(defn enabled-entities
  "Returns all entities tagged with `:entity/enabled?`"
  []
  (filter :entity/enabled? [fractal-canvas rgb-triangle texture (make-noise)]))
