(ns cev.window
  (:import [org.lwjgl.opengl GL GL11]
           [org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback]))

(defn set-key-callback [window]
  (println "ESCAPE KEY=" GLFW/GLFW_KEY_ESCAPE)
  (println "Release code=" GLFW/GLFW_KEY_ESCAPE GLFW/GLFW_RELEASE)
  (GLFW/glfwSetKeyCallback
   window
   ;; Technically this was '.free'd upon exit, not sure if necessary.
   (proxy [GLFWKeyCallback] []
     (invoke [window key scancode action mods]
       (println "GOT KEY" key scancode action mods)
       (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                  (= action GLFW/GLFW_RELEASE))
         (GLFW/glfwSetWindowShouldClose window true))))))

(defn init
  [width height title]
  ;; Technically this callback was .free'd up before exiting, not sure if necessary
  (GLFW/glfwSetErrorCallback (GLFWErrorCallback/createPrint System/err))

  (when-not (GLFW/glfwInit)
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  ;; From example code
  (GLFW/glfwDefaultWindowHints)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)

  ;; From somewhere else
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_CORE_PROFILE)
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_FORWARD_COMPAT GLFW/GLFW_TRUE)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MAJOR 4)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MINOR 1)

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
    #_(GL11/glMatrixMode GL11/GL_PROJECTION)
    #_(GL11/glOrtho 0.0 width
                  0.0 height
                  -1.0 1.0)
    #_(GL11/glMatrixMode GL11/GL_MODELVIEW)

    window))
