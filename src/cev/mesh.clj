(ns cev.mesh
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
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER 0)
    vbo))

(defn- bind-indices [data]
  (let [vbo (GL15/glGenBuffers)
        buffer (make-int-buffer data)]
    ;; GL_ELEMENT_ARRAY_BUFFER is for Vertex array indices
    (GL15/glBindBuffer GL15/GL_ELEMENT_ARRAY_BUFFER vbo)
    (GL15/glBufferData GL15/GL_ELEMENT_ARRAY_BUFFER  buffer GL15/GL_STATIC_DRAW)
    vbo))

(defn- gen-vao []
  (let [vao (GL30/glGenVertexArrays)]
    (GL30/glBindVertexArray vao)
    vao))

(defn create
  "Creates a mesh and loads data to GL.
  1. Creates a VAO for the mesh
  2. Generates a VBO and uploads the vertices
  3. Generates an index buffer and uploads the indices

  Returns an object with the :vao :vbo :idx and :vertex-count"
  [program vertices indices]
  (let [vao (gen-vao)
        vbo (store-data program "point" 2 vertices)
        idx (bind-indices indices)]
    (GL30/glBindVertexArray 0)
    {:vao vao
     :vbo vbo
     :idx idx
     :vertex-count (count indices)}))

(defn delete [mesh]
  (println "Deleting mesh" mesh)
  (GL15/glDeleteBuffers (:vbo mesh))
  (GL15/glDeleteBuffers (:idx mesh))
  (GL30/glDeleteVertexArrays (:vao mesh)))

(defn draw [mesh]
  (GL30/glBindVertexArray (:vao mesh))
  (GL11/glDrawElements GL11/GL_TRIANGLES (:vertex-count mesh) GL11/GL_UNSIGNED_INT 0)
  (GL30/glBindVertexArray 0))
