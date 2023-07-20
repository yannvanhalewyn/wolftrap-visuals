(ns cev.gl.mesh
  (:require [cev.gl.shader :as shader])
  (:import
   [org.lwjgl BufferUtils]
   [org.lwjgl.opengl GL11 GL12 GL13 GL15 GL20 GL30]))

(defn buffer-maker [create]
  (fn make-buffer--x [data]
     (let [buffer (create (count data))]
       (.put buffer data)
       (.flip buffer)
       buffer)))

(def make-float-buffer
  (comp (buffer-maker #(BufferUtils/createFloatBuffer %)) float-array))

(def make-int-buffer
  (comp (buffer-maker #(BufferUtils/createIntBuffer %)) int-array))

(defn- setup-attrib-pointers! [program attributes]
  (let [stride (apply + (map :glsl/dimensions attributes))]
    (loop [[attr & others] attributes
           offset 0]
      (let [dimensions (:glsl/dimensions attr)
            attr-location (GL20/glGetAttribLocation program (:glsl/name attr))]

        (when (= attr-location -1)
          (println "ERROR: could not find attribute " (:glsl/name attr)))
        (GL20/glVertexAttribPointer
         attr-location dimensions GL11/GL_FLOAT false
         (* stride Float/BYTES) (* offset Float/BYTES))
        (GL20/glEnableVertexAttribArray attr-location)

        (when (seq others)
          (recur others (+ offset dimensions)))))))

(defn- store-data [program vertices attributes]
  (let [vbo (GL15/glGenBuffers)
        buffer (make-float-buffer vertices)]
    ;; GL_ARRAY_BUFFER is for Vertex Attributes
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER vbo)
    (GL15/glBufferData GL15/GL_ARRAY_BUFFER buffer GL15/GL_STATIC_DRAW)
    (setup-attrib-pointers! program attributes)

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

(defn- load-texture [program pixels attr-name]
  (let [tex (GL11/glGenTextures)
        pixel-buffer (make-float-buffer pixels)]
    (GL13/glActiveTexture GL13/GL_TEXTURE0)
    (GL11/glBindTexture GL11/GL_TEXTURE_2D tex)
    (GL20/glUniform1i (GL20/glGetUniformLocation program attr-name) 0)
    (GL11/glTexImage2D GL11/GL_TEXTURE_2D 0 GL11/GL_RGB 2 2 0 GL12/GL_BGR GL11/GL_FLOAT pixel-buffer)
    (GL11/glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_WRAP_S GL13/GL_CLAMP_TO_EDGE)
    (GL11/glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MIN_FILTER GL11/GL_NEAREST)
    (GL11/glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MAG_FILTER GL11/GL_NEAREST)
    (GL30/glGenerateMipmap GL11/GL_TEXTURE_2D)
    tex))

(defn- gen-vao []
  (let [vao (GL30/glGenVertexArrays)]
    (GL30/glBindVertexArray vao)
    vao))

(defn load!
  "Creates a mesh and loads data to GL.
  1. Compiles the GLSL program
  2. Creates a VAO for the mesh
  3. Generates a VBO and uploads the vertices
  4. Generates an index buffer and uploads the indices

  Returns a graphics entity with the program, vao, vbo, idx, tex, and
  vertex-count"
  [{:keys [:mesh/vertices :mesh/indices :mesh/texture :glsl/attributes]
    :as entity}]
  (when-let [program (shader/load! entity)]
    (let [vao (gen-vao)
          vbo (store-data program vertices attributes)
          idx (bind-indices indices)
          tex (when-let [{:keys [:texture/pixels :glsl/name]} texture]
                (load-texture program pixels name))]
      (println
       (format "Compiled entity %s, program-id: %d | vao: %d | vertex count: %d"
               (:entity/id entity) program vao (count indices)))
      (GL30/glBindVertexArray 0)
      {:gl/id (random-uuid)
       :gl/program program
       :gl/vao vao
       :gl/vbo vbo
       :gl/idx idx
       :gl/tex tex
       :gl/vertex-count (count indices)})))

(defn destroy! [gl-entity]
  (println "Deleting mesh" gl-entity)
  (GL15/glDeleteBuffers (:gl/vbo gl-entity))
  (GL15/glDeleteBuffers (:gl/idx gl-entity))
  (when-let [tex (:gl/tex gl-entity)]
    (GL11/glBindTexture GL11/GL_TEXTURE_2D 0)
    (GL11/glDeleteTextures tex))
  (GL30/glDeleteVertexArrays (:gl/vao gl-entity))
  (shader/delete! (:gl/program gl-entity)))

(defn draw! [gl-entity]
  (shader/use! (:gl/program gl-entity))
  (GL30/glBindVertexArray (:gl/vao gl-entity))
  (when-let [tex (:gl/tex gl-entity)]
    (GL11/glBindTexture GL11/GL_TEXTURE_2D tex))
  (GL11/glDrawElements GL11/GL_TRIANGLES (:gl/vertex-count gl-entity) GL11/GL_UNSIGNED_INT 0)
  (GL30/glBindVertexArray 0))
