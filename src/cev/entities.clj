(ns cev.entities
  (:require
   [cev.db :as db]
   [cev.engine.entity :as entity]
   [cev.engine.shader :as shader]
   [cev.engine.noise :as noise]
   [medley.core :as m]))

(def fractal-canvas
  (entity/make
   {:entity/name "Fractal Canvas"

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
    [{:glsl/name "point" :glsl/dimensions 3}
     {:glsl/name "texcoord" :glsl/dimensions 2}]}))

(defn make-noise []
  (entity/make
   {:entity/name "Noise"
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

(defmethod db/handle-event ::set
  [{:keys [db]} [_ new-entities]]
  (let [old-gl-entities (vals (:db/gl-entities db))]
    {:db (assoc db :db/entities (m/index-by :entity/id new-entities))
     :gl/enqueue (concat
                  (for [new-entity new-entities]
                    [:gl/compile-entity new-entity])
                  (for [old-gl-entity old-gl-entities]
                    [:gl/destroy-entity old-gl-entity]))}))

(defmethod db/handle-event ::clear
  [{:keys [db]} _]
  {:gl/enqueue (for [gl-entity (vals (:db/gl-entities db))]
                 [:gl/destroy-entity gl-entity])})

(defmethod db/handle-event :gl/loaded-gl-entity
  [{:keys [db]} [_ entity-id gl-entity]]
  {:db (-> db
           (assoc-in [:db/gl-entities (:gl/id gl-entity)] gl-entity)
           (assoc-in [:db/entities entity-id :gl/id] (:gl/id gl-entity)))})

(defmethod db/handle-event :gl/destroyed-gl-entity
  [{:keys [db]} [_ gl-entity-id]]
  {:db (m/dissoc-in db [:db/gl-entities gl-entity-id])})

(defmethod db/read ::all
  [{:db/keys [entities gl-entities]} _]
  (for [entity (vals entities)]
    [entity (get gl-entities (:gl/id entity))]))
