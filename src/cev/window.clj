(ns cev.window
  (:import
   [org.lwjgl BufferUtils]
   [org.lwjgl.glfw GLFW GLFWErrorCallback GLFWKeyCallback]
   [org.lwjgl.opengl GL GL11]))

(defn get-size [window]
  (let [width (BufferUtils/createIntBuffer 1)
        height (BufferUtils/createIntBuffer 1)]
    (GLFW/glfwGetFramebufferSize window width height)
    [(.get width 0) (.get height 0)]))

(defn set-key-callback [window callback]
  (GLFW/glfwSetKeyCallback
   window
   ;; Technically this was '.free'd upon exit, not sure if necessary.
   (proxy [GLFWKeyCallback] []
     (invoke [window key scancode action mods]
       (callback window key scancode action mods)))))

(defn init
  [{::keys [width height title key-callback]}]
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

    (set-key-callback window key-callback)

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

    window))
