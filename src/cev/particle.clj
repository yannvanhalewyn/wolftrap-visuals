(ns cev.particle
  (:require
   [cev.db :as db]
   [cev.engine.entity :as entity]
   [cev.engine.math :as math]
   [cev.engine.shader :as shader]
   [cev.engine.window :as window]
   [cev.renderer :as renderer]))

(defn- make-particles [num-particles width height]
  (let [width 1 height 1
        speed 20
        max-age 20]
    (repeatedly
     num-particles
     (fn []
       {:particle/position [(math/random (- width) width)
                            (math/random (- height) height)]
        :particle/velocity [(math/random (- speed) speed)
                            (math/random (- speed) speed)]
        :particle/age 0
        :particle/max-age max-age}))))

(defn make-entity []
  (entity/make
   {:entity/name "Particle"
    :gl.renderer/id ::renderer

    :mesh/vertices
    [-1.0 -1.0
     -1.0  1.0
      1.0  1.0
      1.0 -1.0]

    :mesh/indices
    [0 1 2 0 2 3]

    :glsl/vertex-source (shader/resource-file "particle.vert")
    :glsl/fragment-source (shader/resource-file "particle.frag")

    :glsl/attributes
    [{:glsl/name "uv" :glsl/dimensions 2}]}))

(defmethod db/handle-event ::init
  [{:keys [db ::window/width ::window/height]} [_ num-particles]]
  {:db (assoc db ::particles (make-particles num-particles width height))
   :dispatch [[::renderer/add (make-entity)]]})

(defmethod db/handle-event ::clear
  [{:keys [db]} _]
  {:db (dissoc db ::particles)
   :dispatch [[::renderer/clear]]})

(defmethod db/read ::particles
  [{::keys [particles] :as db} _]
  (when particles
    [particles (renderer/get-renderer db ::renderer)]))
