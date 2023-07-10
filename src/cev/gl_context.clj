(ns cev.gl-context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.shader :as shader]
   [cev.vertex-array :as vertex-array]
   [cev.window :as window])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11 GL30]))

(def vertices (float-array [-0.5 -0.5 0.0
                            0.5 -0.5 0.0
                            0.0 0.5 0.0]))

(def indices (int-array [0 1 2]))

(defn run!
  [width height]
  (try
    (let [window (window/init width height "let there be triangles")
          program (shader/load "triangle")
          mesh (vertex-array/create-mesh program vertices indices)]
      (println "PROGRAM" program)

      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (while (not (GLFW/glfwWindowShouldClose window))
        (GL11/glClearColor 0.0 0.0 0.0 0.0)
        (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
        (shader/use program)
        (vertex-array/draw-mesh mesh)
        (GLFW/glfwSwapBuffers window)
        (GLFW/glfwPollEvents))

      (shader/delete program)
      (GLFW/glfwDestroyWindow window))
    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))

(comment
  (future (Thread/sleep 5000)
          (println "Loading shader")
          (shader/load))
  (a/go
    (a/<! (a/timeout 5000))
    (println "Running block")
    (shader/load "blue")))
