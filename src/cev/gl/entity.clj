(ns cev.gl.entity
  (:import
   [org.lwjgl.glfw GLFW]))

(defn make [attrs]
  (merge {:entity/id (random-uuid)} attrs))
