(ns cev.gl.context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.gl.entity :as entity]
   [cev.gl.mesh :as mesh]
   [cev.gl.shader :as shader]
   [cev.gl.window :as window]
   [cev.midi :as midi])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

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

(defn run!
  [window-opts entities]
  (try
    (let [window (window/init window-opts)]
      (db/set-window! window)

      (doseq [entity entities]
        (db/add-entity! (entity/compile! entity)))

      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (GL11/glEnable GL11/GL_BLEND)
      (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)

      (while (not (GLFW/glfwWindowShouldClose window))
        (draw!))

      (doseq [entity (db/entities)]
        (shader/delete (:entity/program entity)))
      (GLFW/glfwDestroyWindow window))

    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))
