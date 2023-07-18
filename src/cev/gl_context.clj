(ns cev.gl-context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.mesh :as mesh]
   [cev.midi :as midi]
   [cev.shader :as shader]
   [cev.window :as window])
  (:import
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

;; (def vertices
;;   [-1.0 -1.0
;;    -1.0  1.0
;;     1.0  1.0
;;     1.0 -1.0])

;; (def indices [0 1 2 0 2 3])

(def vertices
  [-1.0 -1.0 0.0 1.0 0.0 0.0
    0.0  1.0 0.0 0.0 1.0 0.0
    1.0 -1.0 0.0 0.0 0.0 1.0])

(def indices [0 1 2])

(def glsl-attrs
  [{:glsl/name "vpos"
    :glsl/dimensions 3}
   {:glsl/name "vcol"
    :glsl/dimensions 3}])

(defn- draw! []
  (let [window (db/get :window)
        program (db/get :program)
        mesh (db/get :mesh)
        [width height] (window/get-size window)]
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))
    (shader/use program)
    (shader/uniform-2f program "resolution" width height)
    (shader/uniform-1f program "iterations" (midi/normalize (db/midi-cc 72) 1.0 20.0))
    (shader/uniform-1f program "complexity" (midi/normalize (db/midi-cc 79)))
    (shader/uniform-1f program "brightness" (midi/normalize (db/midi-cc 91) 0.01 1.0))
    (shader/uniform-1f program "time" (GLFW/glfwGetTime))
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
      (let [[vertex-shader fragment-shader] (db/current-shaders)]
        (when-let [program (shader/load vertex-shader fragment-shader)]
          (let [mesh (mesh/create program vertices indices glsl-attrs)]
            (shader/delete (db/get :program))
            (mesh/delete (db/get :mesh))
            (db/set-mesh! program mesh))))

      nil)))

(defn run!
  [width height]
  (try
    (db/set-shaders! "playground" "playground")
    (let [window (window/init
                  {::window/width width
                   ::window/height height
                   ::window/title "Wolftrap Visuals"
                   ::window/key-callback key-callback})
          [vertex-shader fragment-shader] (db/current-shaders)
          program (shader/load vertex-shader fragment-shader)
          mesh (mesh/create program vertices indices glsl-attrs)]

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
