(ns cev.gl.entity)

(defn make [attrs]
  (merge {:entity/id (random-uuid)} attrs))
