(ns cev.gl-context
  (:require
   [cev.shader :as shader]
   [cev.window :as window])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL GL11]))

(defn run!
  [width height]
  (try
    (let [window (window/init 800 600 "alpha")
          program (shader/load "red")]
      ;; (vertex-array/init program)
      (while (not (GLFW/glfwWindowShouldClose window))
        (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
        (shader/use program)
        (GL11/glEnd)
        ;; (vertex-array/draw)
        (GLFW/glfwSwapBuffers window)
        (GLFW/glfwPollEvents))

      (shader/delete program)
      (GLFW/glfwDestroyWindow window))
    (finally
      (GLFW/glfwTerminate))))


(comment
  (future (Thread/sleep 5000)
          (println "Loading shader")
          (shader/load))
  (a/go
    (a/<! (a/timeout 5000))
    (println "Running block")
    (shader/load "blue")))
