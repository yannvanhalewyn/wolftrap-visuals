(ns cev.entity
  (:require
   [cev.mesh :as mesh]
   [cev.shader :as shader]))

(defn make [attrs]
  (merge {:entity/id (random-uuid)} attrs))

(defn compile! [entity]
  (when-let [program (shader/load entity)]
    (let [mesh (mesh/create program entity)]
      (println (format "Compiled entity %s, program-id: %d | mesh: %s"
                       (:entity/id entity) program (pr-str mesh)))
      (assoc entity
        :entity/program program
        :entity/mesh mesh))))

(defn re-compile! [entity]
  (when-let [new-entity (compile! entity)]
    (shader/delete (:entity/program entity))
    (mesh/delete (:entity/mesh entity))
    new-entity))
