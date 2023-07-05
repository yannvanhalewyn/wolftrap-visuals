(ns cev.window
  (:require [cev.shader :as shader]
            [cev.vertex-array :as vertex-array]
            [clojure.core.async :as a])
  (:import [org.lwjgl.opengl GL GL11]
           [org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback]))

(defn set-key-callback [window]
  (GLFW/glfwSetKeyCallback
   window
   ;; Technically this was '.free'd upon exit, not sure if necessary.
   (proxy [GLFWKeyCallback] []
     (invoke [window key scancode action mods]
       (println "GOT KEY" key scancode action mods)
       (shader/load "blue")
       (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                  (= action GLFW/GLFW_RELEASE))
         (GLFW/glfwSetWindowShouldClose window true))))))

(defonce cleanup (atom []))

(defn init
  [width height title]
  ;; Technically this callback was .free'd up before exiting, not sure if necessary
  (GLFW/glfwSetErrorCallback (GLFWErrorCallback/createPrint System/err))

  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (let [window (GLFW/glfwCreateWindow width height title 0 0)]
    (when-not window
      (throw (RuntimeException. "Failed to create the GLFW window")))

    (set-key-callback window)

    (let [vidmode (GLFW/glfwGetVideoMode (GLFW/glfwGetPrimaryMonitor))]
      (GLFW/glfwSetWindowPos
       window
       (/ (- (.width vidmode) width) 2)
       (/ (- (.height vidmode) height) 2))
      (GLFW/glfwMakeContextCurrent window)
      (GLFW/glfwSwapInterval 1)
      (GLFW/glfwShowWindow window))

    (GL/createCapabilities)
    (println "OpenGL version:" (GL11/glGetString GL11/GL_VERSION))
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glMatrixMode GL11/GL_PROJECTION)
    (GL11/glOrtho 0.0 width
                  0.0 height
                  -1.0 1.0)
    (GL11/glMatrixMode GL11/GL_MODELVIEW)

    window))

(defn init2 []
  (GLFW/glfwInit)
  (let [window (GLFW/glfwCreateWindow 800 600 "My Window" 0 0)]
    (GLFW/glfwMakeContextCurrent window)
    (GL/createCapabilities)
    window))
