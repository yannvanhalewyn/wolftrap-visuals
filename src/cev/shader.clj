(ns cev.shader
  (:refer-clojure :exclude [use])
  (:import [org.lwjgl BufferUtils]
           [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL GL11 GL12 GL13 GL15 GL20 GL30]))

(def vertex-source "
//the position of the vertex as specified by our renderer
attribute vec3 Position;

void main() {
    //pass along the position
    gl_Position = vec4(Position, 1.0);
}
")

(def fragment-source "
void main() {
    //pass along the color red
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
")

(defn make-shader [source shader-type]
  (let [shader (GL20/glCreateShader shader-type)]
    (GL20/glShaderSource shader source)
    (GL20/glCompileShader shader)
    (when (zero? (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))
      (throw (Exception. (GL20/glGetShaderInfoLog shader 1024))))
    shader))

(defn make-program [vertex-shader fragment-shader]
  (let [program (GL20/glCreateProgram)]
    (GL20/glAttachShader program vertex-shader)
    (GL20/glAttachShader program fragment-shader)
    (GL20/glLinkProgram program)
    (when (zero? (GL20/glGetProgrami program GL20/GL_LINK_STATUS))
      (throw (Exception. (GL20/glGetProgramInfoLog program 1024))))
    program))

(def vertex-shader
  (delay (make-shader vertex-source GL20/GL_VERTEX_SHADER)))

(def fragment-shader
  (delay (make-shader fragment-source GL20/GL_FRAGMENT_SHADER)))

(def program
  (delay (make-program vertex-shader fragment-shader)))

(defonce program-atom (atom nil))

(defn load []
  (let [vertex-shader (make-shader vertex-source GL20/GL_VERTEX_SHADER)
        fragment-shader (make-shader fragment-source GL20/GL_FRAGMENT_SHADER)
        program (make-program vertex-shader fragment-shader)]
    (reset! program-atom program)))

(defn use []
  (when-let [program @program-atom]
    (GL20/glUseProgram program)))

(defn cleanup []
  (when-let [program @program-atom]
    (GL20/glDeleteProgram program)))
