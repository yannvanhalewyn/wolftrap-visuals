(ns cev.gl.entity
  (:require
   [cev.gl.mesh :as mesh]
   [cev.gl.shader :as shader]))

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