(ns cev.main
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.gl.context :as gl.context]
   [cev.gl.entity :as entity]
   [cev.gl.shader :as shader]
   [cev.gl.window :as window]
   [cev.midi :as midi]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl])
  (:import
   [org.lwjgl.glfw GLFW]))

(defn- handle-midi! [msg]
  (db/handle-midi! msg))

(def fractal-canvas
  {:entity/id :fractal-canvas
   :entity/name "Fractal Canvas"

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
   [{:glsl/name "pos" :glsl/dimensions 2}]})

(def rgb-triangle
  {:entity/id :rgb-triangle
   :entity/name "RGB Triangle"

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
    {:glsl/name "vopacity" :glsl/dimensions 1}]})

(defn- key-callback [window key scancode action mods]
  (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      ;; TODO would work better if we decouple the compiled shader data from the
      ;; entity data, this way we don't have to merge-with merge e.a
      (do (db/update-entities! [rgb-triangle])
          (doseq [entity (db/entities)]
            (println :entity entity)
            (when-let [new-entity (entity/re-compile! entity)]
              (db/add-entity! new-entity))))

      nil)))

(defn- run! [width height]
  (gl.context/run!
   {::window/width width
    ::window/height height
    ::window/title "Wolftrap Visuals"
    ::window/key-callback key-callback}
   [rgb-triangle #_fractal-canvas]))

(defn -main [& _args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server :port 7888)
  (midi/add-listener! handle-midi!)
  (run! 800 600)
  (io/delete-file ".nrepl-port"))
