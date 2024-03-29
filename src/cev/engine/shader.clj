(ns cev.engine.shader
  (:refer-clojure :exclude [use load])
  (:require
   [cev.log :as log]
   [clojure.java.io :as io])
  (:import [org.lwjgl.opengl GL20]))

(defn- make-shader [source shader-type]
  (let [shader (GL20/glCreateShader shader-type)]
    (GL20/glShaderSource shader source)
    (GL20/glCompileShader shader)
    (if (zero? (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))
      (log/error :shader/compilation-error (GL20/glGetShaderInfoLog shader 1024))
      shader)))

(defn- make-program [vertex-shader fragment-shader]
  (let [program (GL20/glCreateProgram)]
    (GL20/glAttachShader program vertex-shader)
    (GL20/glAttachShader program fragment-shader)
    ;; (GL20/glBindAttribLocation program 0 "resolution")
    (GL20/glLinkProgram program)
    (if (zero? (GL20/glGetProgrami program GL20/GL_LINK_STATUS))
      (log/error :shader/linking-error (GL20/glGetProgramInfoLog program 1024))
      program)))

(defn resource-file [shader-name]
  (io/resource (str "cev/shaders/" shader-name)))

(defn load! [{:keys [:glsl/vertex-source :glsl/fragment-source]}]
  (let [vertex-shader (make-shader (slurp vertex-source) GL20/GL_VERTEX_SHADER)
        fragment-shader (make-shader (slurp fragment-source) GL20/GL_FRAGMENT_SHADER)]
    (when (and vertex-shader fragment-shader)
      (make-program vertex-shader fragment-shader))))

(defn enable! [program]
  (GL20/glUseProgram program))

(defn disable! []
  (GL20/glUseProgram 0))

(defn delete! [program]
  (GL20/glDeleteProgram program))

(defn- uniform-location [program name]
  (let [loc (GL20/glGetUniformLocation program name)]
    (when-not (= loc -1)
      loc)))

(defn uniform-1f [program name x]
  (when-let [loc (uniform-location program name)]
    (GL20/glUniform1f loc x)))

(defn uniform-2f
  ([program gl-name vec]
   (uniform-2f program gl-name (.getX vec) (.getY vec)))
  ([program name x y]
   (when-let [loc (uniform-location program name)]
     (GL20/glUniform2f loc x y))))

(defn uniform-transmitter
  "Returns a function that will execute the GL calls in order to load the
  uniforms a program given an extraction function from a collection."
  [uniform-descriptors]
  (println uniform-descriptors)
  (fn [program coll]
    (doseq [[gl-name data-type extract-fn] uniform-descriptors
          :let [value (extract-fn coll)]]
      (case data-type
        :1f (uniform-1f program gl-name value)
        :2f (uniform-2f program gl-name value)))))
