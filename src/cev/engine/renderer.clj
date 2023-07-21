(ns cev.engine.renderer
  (:require
   [cev.log :as log]
   [cev.engine.math :as math]
   [cev.engine.shader :as shader])
  (:import
   [org.lwjgl BufferUtils]
   [org.lwjgl.opengl GL11 GL13 GL15 GL20 GL30]))

(defn buffer-maker [create]
  (fn make-buffer--x [data]
     (let [buffer (create (count data))]
       (.put buffer data)
       (.flip buffer)
       buffer)))

(def ^:private make-float-buffer
  (comp (buffer-maker #(BufferUtils/createFloatBuffer %)) float-array))

(def ^:private make-int-buffer
  (comp (buffer-maker #(BufferUtils/createIntBuffer %)) int-array))

(defn- setup-attrib-pointers! [program attributes]
  (let [stride (apply + (map :glsl/dimensions attributes))]
    (loop [[attr & others] attributes
           offset 0]
      (let [dimensions (:glsl/dimensions attr)
            attr-location (GL20/glGetAttribLocation program (:glsl/name attr))]

        (when (= attr-location -1)
          (log/error :gl/shader "Could not find attribute" (:glsl/name attr)))
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

(def ^:private texture-formats
  {:gl/rgb GL11/GL_RGB
   :gl/depth-component GL11/GL_DEPTH_COMPONENT})

(defn- get-texture-format [format]
  (or (get texture-formats format)
      (throw (ex-info "Unknown texture format" {:texture/format format}))))

(defn- load-texture
  [program {:texture/keys [pixels width height format] :keys [:glsl/name]}]
  (let [tex (GL11/glGenTextures)
        pixel-buffer (make-float-buffer pixels)
        texture-format (get-texture-format format)]
    (GL13/glActiveTexture GL13/GL_TEXTURE0)
    (GL11/glBindTexture GL11/GL_TEXTURE_2D tex)
    (GL20/glUniform1i (GL20/glGetUniformLocation program name) 0)
    (GL11/glTexImage2D GL11/GL_TEXTURE_2D 0 texture-format width height 0
                       texture-format GL11/GL_FLOAT pixel-buffer)
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
          tex (when texture
                (load-texture program texture))]
      (log/info
       :renderer/compiled
       (format
        "Compiled entity %s, program-id: %d | vao: %d | vertex count: %d"
        (:entity/id entity) program vao (count indices)))
      (GL30/glBindVertexArray 0)
      {:gl/id (random-uuid)
       :gl/program program
       :gl/vao vao
       :gl/vbo vbo
       :gl/idx idx
       :gl/tex tex
       :gl/vertex-count (count indices)})))

(defn destroy!
  "Cleanes up a renderer by deleting all OpenGL buffers, textures and shaders"
  [renderer]
  (GL15/glDeleteBuffers (:gl/vbo renderer))
  (GL15/glDeleteBuffers (:gl/idx renderer))
  (when-let [tex (:gl/tex renderer)]
    (GL11/glBindTexture GL11/GL_TEXTURE_2D 0)
    (GL11/glDeleteTextures tex))
  (GL30/glDeleteVertexArrays (:gl/vao renderer))
  (shader/delete! (:gl/program renderer))
  (log/info :renderer/destroyed renderer))

(defn bind-uniform-1f
  "Binds a vec2 uniform to the renderer's shader"
  [renderer attr-name v]
  (shader/uniform-1f (:gl/program renderer) attr-name v))

(defn bind-uniform-2f
  "Binds a vec2 uniform to the renderer's shader"
  [renderer attr-name v]
  (shader/uniform-2f (:gl/program renderer) attr-name (math/x v) (math/y v)))

(defn mount!
  "Prepares the renderer for being used"
  [renderer]
  (shader/enable! (:gl/program renderer))
  (GL30/glBindVertexArray (:gl/vao renderer))
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE)
  (GL11/glDepthMask false)
  (when-let [tex (:gl/tex renderer)]
    (GL11/glBindTexture GL11/GL_TEXTURE_2D tex)))

(defn unmount!
  "Unmounts the renderer"
  []
  (shader/disable!)
  (GL30/glBindVertexArray 0))

(defmacro batch
  "Mounts the renderer and executes the body before unmounting."
  [renderer & body]
  `(do (mount! ~renderer)
       ~@body
       (unmount!)))

(defn draw-one! [renderer]
  ;; Use GL11/GL_TRIANGLE_STRIP for lines I guess
  (GL11/glDrawElements
   GL11/GL_TRIANGLES (:gl/vertex-count renderer) GL11/GL_UNSIGNED_INT 0))

(defn draw! [renderer]
  (batch renderer (draw-one! renderer)))
