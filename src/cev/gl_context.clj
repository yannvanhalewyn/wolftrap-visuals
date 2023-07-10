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

(defonce state (atom {}))

(defn- draw! [{:keys [window program mesh]}]
  (GL11/glClearColor 0.0 0.0 0.0 0.0)
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
  (shader/use program)
  (vertex-array/draw-mesh mesh)
  (GLFW/glfwSwapBuffers window)
  (GLFW/glfwPollEvents))

(defn- key-callback [window key scancode action mods]
  (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_ESCAPE
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      (when-let [new-program (shader/load "triangle")]
        (shader/delete (:program @state))
        (swap! state assoc :program new-program))

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
          mesh (vertex-array/create-mesh program vertices indices)]


      (swap! state assoc
             :window window
             :program program
             :mesh mesh)

      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (while (not (GLFW/glfwWindowShouldClose window))
        (draw! @state))

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
