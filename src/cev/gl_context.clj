(ns cev.gl-context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.entity :as entity]
   [cev.mesh :as mesh]
   [cev.midi :as midi]
   [cev.shader :as shader]
   [cev.window :as window])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

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
    :mesh/vertices
    [-1.0 -1.0 0.0 1.0 0.0 0.0
     0.0  1.0 0.0 0.0 1.0 0.0
     1.0 -1.0 0.0 0.0 0.0 1.0]
    :mesh/indices
    [0 1 2]

    :glsl/vertex-source (shader/resource-file "playground.vert")
    :glsl/fragment-source (shader/resource-file "playground.frag")
    :glsl/attributes
    [{:glsl/name "vpos" :glsl/dimensions 3}
     {:glsl/name "vcol" :glsl/dimensions 3}]}))

(defn- draw! []
  (let [window (db/get :window)
        [width height] (window/get-size window)]
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))

    (doseq [{:entity/keys [mesh program]} (db/entities)]
      (shader/use program)
      (shader/uniform-2f program "resolution" width height)
      (shader/uniform-1f program "iterations" (midi/normalize (db/midi-cc 72) 1.0 20.0))
      (shader/uniform-1f program "complexity" (midi/normalize (db/midi-cc 79)))
      (shader/uniform-1f program "brightness" (midi/normalize (db/midi-cc 91) 0.01 1.0))
      (shader/uniform-1f program "time" (GLFW/glfwGetTime))
      (mesh/draw mesh))

    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

(defn- key-callback [window key scancode action mods]
  (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      (doseq [entity (db/entities)]
        (when-let [new-entity (entity/re-compile! entity)]
          (db/add-entity! new-entity)))

      nil)))

(defn run!
  [width height]
  (try
    (let [window (window/init
                  {::window/width width
                   ::window/height height
                   ::window/title "Wolftrap Visuals"
                   ::window/key-callback key-callback})
          entity1 (entity/compile! fractal-canvas)
          entity2 (entity/compile! rgb-triangle)]

      (db/set-window! window)
      (db/add-entity! entity1)
      (db/add-entity! entity2)

      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (while (not (GLFW/glfwWindowShouldClose window))
        (draw!))

      (doseq [entity (db/entities)]
        (shader/delete (:entity/program entity)))
      (GLFW/glfwDestroyWindow window))
    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))

(comment
  (a/go
    (a/<! (a/timeout 5000))
    (println "Running block")
    (shader/load "blue")))
