(ns cev.gl-context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.mesh :as mesh]
   [cev.shader :as shader]
   [cev.window :as window])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

(def vertices
  [-1.0  1.0
    1.0  1.0
   -1.0 -1.0
    1.0  1.0
   -1.0 -1.0
    1.0 -1.0])

(def indices [0 1 2 3 4 5])

(defn- draw! []
  (let [window (db/get :window)
        program (db/get :program)
        mesh (db/get :mesh)
        [width height] (window/get-size window)]
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    (shader/use program)
    (shader/uniform-2f program "resolution" width height)
    (shader/uniform-1f program "slider" (float (/ (:value (db/get :midi)) 127)))
    (mesh/draw mesh)
    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

(defn- key-callback [window key scancode action mods]
  (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      (when-let [program (shader/load "triangle")]
        (let [mesh (mesh/create program vertices indices)]
          (shader/delete (db/get :program))
          (mesh/delete (db/get :mesh))
          (db/set-mesh! program mesh)))

      nil)))

(defn run!
  [width height]
  (try
    (let [window (window/init
                  {::window/width width
                   ::window/height height
                   ::window/title "let there be triangles"
                   ::window/key-callback key-callback})
          program (shader/load "triangle")
          mesh (mesh/create program vertices indices)]

      (db/set-window! window)
      (db/set-mesh! program mesh)

      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (while (not (GLFW/glfwWindowShouldClose window))
        (draw!))

      (shader/delete program)
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
