(ns cev.particle
  (:require
   [cev.db :as db]
   [cev.engine.entity :as entity]
   [cev.engine.math :as math]
   [cev.engine.shader :as shader]
   [cev.engine.window :as window]
   [cev.renderer :as renderer]))

(defn- make-particle [position velocity size max-age]
  {:particle/position position
   :particle/velocity velocity
   :particle/size size
   :particle/age 0
   :particle/max-age max-age})

(defn- make-random-particle [[width height] speed max-age]
  (make-particle
   (math/vec2
    (math/random (- width) width)
    (math/random (- height) height))
   (math/vec2
    (math/random (- speed) speed)
    (math/random (- speed) speed))
   (math/random 10 100)
   max-age))

(defn- make-particles [num-particles _resolution]
  (let [resolution [1 1]
        velocity 20
        max-age 20
        make-random #(make-random-particle resolution velocity max-age)]
    (repeatedly num-particles make-random)))

(defn make-gl-blueprint []
  (entity/make
   {:entity/name "Particle"
    ::renderer/batch-id ::renderer

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
    [{:glsl/name "uv" :glsl/dimensions 2}]

    :glsl/uniform-transmitter
    (shader/uniform-transmitter
     [["resolution" :2f #(apply math/->Vec2 (::window/resolution %))]
      ["position"   :2f :particle/velocity]
      ["size"       :1f :particle/size]])}))

(defn update! [{:particle/keys [position velocity]} dt]
  (.setX position (+ (.getX position) (/ (.getX velocity) 1000)))
  (.setY position (+ (.getY position) (/ (.getY velocity) 1000))))

(defmethod db/handle-event ::init
  [{:keys [db ::window/resolution]} [_ num-particles]]
  {:db (assoc db ::particles (make-particles num-particles resolution))
   :dispatch [[::renderer/clear]
              [::renderer/add (make-gl-blueprint)]]})

(defmethod db/handle-event ::clear
  [{:keys [db]} _]
  {:db (dissoc db ::particles)
   :dispatch [[::renderer/clear]]})

(defmethod db/read ::particles
  [{::keys [particles] :as db} _]
  (when particles
    [particles (renderer/get-renderer db ::renderer)]))
