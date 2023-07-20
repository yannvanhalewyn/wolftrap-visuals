(ns cev.engine.entity)

(defn make [attrs]
  (merge {:entity/id (random-uuid)} attrs))
