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

(defn- store-data [program attr-name dimensions data]
  (let [vbo (GL15/glGenBuffers)
        buffer (make-float-buffer data)
        attr-location (GL20/glGetAttribLocation program attr-name)]
    ;; GL_ARRAY_BUFFER is for Vertex Attributes
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo)
    (GL15/glBufferData GL15/GL_ARRAY_BUFFER buffer GL15/GL_STATIC_DRAW)

    (println "ATTR" attr-name attr-location)
    (GL20/glVertexAttribPointer attr-location dimensions GL11/GL_FLOAT false 0 0)
    (GL20/glEnableVertexAttribArray attr-location)
    ;; Unload VBO when done
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)))

(defn- bind-indices [data]
  (let [vbo (GL15/glGenBuffers)
        buffer (make-int-buffer data)]
    ;; GL_ELEMENT_ARRAY_BUFFER is for Vertex array indices
    (GL15/glBindBuffer GL15/GL_ELEMENT_ARRAY_BUFFER vbo)
    (GL15/glBufferData GL15/GL_ELEMENT_ARRAY_BUFFER  buffer GL15/GL_STATIC_DRAW)))

(defn- gen-vao []
  (let [vao (GL30/glGenVertexArrays)]
    (GL30/glBindVertexArray vao)
    vao))

(defn create-mesh
  "Creates and returns a VAO for the mesh.
  1. Generates a VBO and stores the position data it"
  [program positions indices]
  (let [vao (gen-vao)]
    (store-data program "point" 3 positions)
    (bind-indices indices)
    (GL30/glBindVertexArray 0)
    {:vao vao :length (count indices)}))

(defn draw-mesh [mesh]
  (GL30/glBindVertexArray (:vao mesh))
  (GL11/glDrawElements GL11/GL_TRIANGLES (:length mesh) GL11/GL_UNSIGNED_INT 0)
  (GL30/glBindVertexArray 0))

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
    (GLFW/glfwTerminate))


  )
