(ns cev.vertex-array
  (:import [org.lwjgl BufferUtils]
           [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL GL11 GL12 GL13 GL15 GL20 GL30]))

(defn buffer-maker [create-fn]
  (fn make-buffer--x [data]
     (let [buffer (create-fn (count data))]
       (.put buffer data)
       (.flip buffer)
       buffer)))

(def make-float-buffer (buffer-maker #(BufferUtils/createFloatBuffer %)))
(def make-int-buffer (buffer-maker #(BufferUtils/createIntBuffer %)))

(defn draw []
  (GL11/glDrawElements GL11/GL_TRIANGLES 3 GL11/GL_UNSIGNED_INT 0))

(defn init [program]
  (let [vertices (float-array [ 0.5  0.5 0.0 1.0 1.0
                               -0.5  0.5 0.0 0.0 1.0
                               -0.5 -0.5 0.0 0.0 0.0])
        indices (int-array [0 1 2])

        vao (GL30/glGenVertexArrays)
        vbo (GL15/glGenBuffers)

        vertices-buffer (make-float-buffer vertices)
        indices-buffer (make-int-buffer indices)]
    ;; (sc.api/spy )
    (GL30/glBindVertexArray vao)
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo)

    (GL15/glBufferData
     GL15/GL_ARRAY_BUFFER vertices-buffer GL15/GL_STATIC_DRAW)

    (let [idx (GL15/glGenBuffers)]
      (GL15/glBindBuffer GL15/GL_ELEMENT_ARRAY_BUFFER idx))

    (GL15/glBufferData
     GL15/GL_ELEMENT_ARRAY_BUFFER indices-buffer GL15/GL_STATIC_DRAW)

    (GL20/glVertexAttribPointer (GL20/glGetAttribLocation program "point"   ) 3 GL11/GL_FLOAT false (* 5 Float/BYTES) (* 0 Float/BYTES))
    (GL20/glVertexAttribPointer (GL20/glGetAttribLocation program "texcoord") 2 GL11/GL_FLOAT false (* 5 Float/BYTES) (* 3 Float/BYTES))
    (GL20/glEnableVertexAttribArray 0)
    (GL20/glEnableVertexAttribArray 1)

    (GL20/glUseProgram program)

    (GL11/glEnable GL11/GL_DEPTH_TEST)
    ))

(comment
  (defn cleanup []
    (GL20/glDisableVertexAttribArray 1)
    (GL20/glDisableVertexAttribArray 0)

    (GL11/glBindTexture GL11/GL_TEXTURE_2D 0)
    (GL11/glDeleteTextures tex)

    (GL15/glBindBuffer GL15/GL_ELEMENT_ARRAY_BUFFER 0)
    (GL15/glDeleteBuffers idx)

    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)
    (GL15/glDeleteBuffers vbo)

    (GL30/glBindVertexArray 0)
    (GL30/glDeleteVertexArrays vao)

    (GL20/glDeleteProgram program)

    (GLFW/glfwDestroyWindow window)
    (GLFW/glfwTerminate)))
