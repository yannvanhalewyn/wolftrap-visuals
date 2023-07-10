(ns cev.shader
  (:refer-clojure :exclude [use load])
  (:require [clojure.java.io :as io])
  (:import [org.lwjgl.opengl GL20]))

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

(defn read-shaders [name]
  {:vertex-source (slurp (io/resource (str "cev/shaders/" name ".vert")))
   :fragment-source (slurp (io/resource (str "cev/shaders/" name ".frag")))})

(defn load [name]
  (let [shaders (read-shaders name)
        vertex-shader (make-shader
                       (:vertex-source shaders) GL20/GL_VERTEX_SHADER)
        fragment-shader (make-shader
                         (:fragment-source shaders) GL20/GL_FRAGMENT_SHADER)]
    (println "Loaded shaders, making program...")
    (make-program vertex-shader fragment-shader)))

(defn use [program]
  (GL20/glUseProgram program))

(defn delete [program]
  (GL20/glDeleteProgram program))
