(ns cev.engine.shader
  (:refer-clojure :exclude [use load])
  (:require [clojure.java.io :as io])
  (:import [org.lwjgl.opengl GL20]))

(defn- make-shader [source shader-type]
  (let [shader (GL20/glCreateShader shader-type)]
    (GL20/glShaderSource shader source)
    (GL20/glCompileShader shader)
    (if (zero? (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))
      (println (GL20/glGetShaderInfoLog shader 1024))
      shader)))

(defn- make-program [vertex-shader fragment-shader]
  (let [program (GL20/glCreateProgram)]
    (GL20/glAttachShader program vertex-shader)
    (GL20/glAttachShader program fragment-shader)
    ;; (GL20/glBindAttribLocation program 0 "resolution")
    (GL20/glLinkProgram program)
    (if (zero? (GL20/glGetProgrami program GL20/GL_LINK_STATUS))
      (println (GL20/glGetProgramInfoLog program 1024))
      program)))

(defn- uniform-location [program name]
  (let [loc (GL20/glGetUniformLocation program name)]
    (when-not (= loc -1)
      loc)))

(defn resource-file [shader-name]
  (io/resource (str "cev/gl/shaders/" shader-name)))

(defn load! [{:keys [:glsl/vertex-source :glsl/fragment-source]}]
  (let [vertex-shader (make-shader (slurp vertex-source) GL20/GL_VERTEX_SHADER)
        fragment-shader (make-shader (slurp fragment-source) GL20/GL_FRAGMENT_SHADER)]
    (when (and vertex-shader fragment-shader)
      (make-program vertex-shader fragment-shader))))

(defn use! [program]
  (GL20/glUseProgram program))

(defn delete! [program]
  (GL20/glDeleteProgram program))

(defn uniform-2f [program name x y]
  (when-let [loc (uniform-location program name)]
    (GL20/glUniform2f loc x y)))

(defn uniform-1f [program name x]
  (when-let [loc (uniform-location program name)]
    (GL20/glUniform1f loc x)))
