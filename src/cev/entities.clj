(ns cev.entities
  (:require
   [cev.db :as db]
   [cev.engine.entity :as entity]
   [cev.engine.shader :as shader]
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

(defmethod db/run-event ::set
  [{:keys [db]} [_ new-entities]]
  (let [old-gl-entities (vals (:db/gl-entities db))]
    {:db (assoc db :db/entities (m/index-by :entity/id new-entities))
     :gl/enqueue (concat
                  (for [new-entity new-entities]
                    [:gl/compile-entity new-entity])
                  (for [old-gl-entity old-gl-entities]
                    [:gl/destroy-entity old-gl-entity]))}))

(defmethod db/run-event ::clear
  [{:keys [db]} _]
  {:gl/enqueue (for [gl-entity (vals (:db/gl-entities db))]
                 [:gl/destroy-entity gl-entity])})

(defmethod db/run-event :gl/loaded-gl-entity
  [{:keys [db]} [_ entity-id gl-entity]]
  {:db (-> db
           (assoc-in [:db/gl-entities (:gl/id gl-entity)] gl-entity)
           (assoc-in [:db/entities entity-id :gl/id] (:gl/id gl-entity)))})

(defmethod db/run-event :gl/destroyed-gl-entity
  [{:keys [db]} [_ gl-entity-id]]
  {:db (m/dissoc-in db [:db/gl-entities gl-entity-id])})

(defmethod db/read ::all
  [{:db/keys [entities gl-entities]} _]
  (for [entity (vals entities)]
    [entity (get gl-entities (:gl/id entity))]))
