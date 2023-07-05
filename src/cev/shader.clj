(ns cev.shader
  (:refer-clojure :exclude [use load])
  (:require [clojure.java.io :as io])
  (:import [org.lwjgl BufferUtils]
           [org.lwjgl.glfw GLFW]
           [org.lwjgl.opengl GL GL11 GL12 GL13 GL15 GL20 GL30]))

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
  (println (slurp (io/resource (str "cev/shaders/" name ".frag"))))
  {:vertex-source (slurp (io/resource (str "cev/shaders/" name ".vert")))
   :fragment-source (slurp (io/resource (str "cev/shaders/" name ".frag")))})

(defn use [program]
  (GL20/glUseProgram program))

(defn delete [program]
  (GL20/glDeleteProgram program))

(defn load [name]
  (let [shaders (read-shaders name)
        vertex-shader (make-shader
                       (:vertex-source shaders) GL20/GL_VERTEX_SHADER)
        fragment-shader (make-shader
                         (:fragment-source shaders) GL20/GL_FRAGMENT_SHADER)]
    (make-program vertex-shader fragment-shader)))

(comment

  (def vertex-source (slurp (io/resource "cev/shaders/simple.vert")))

  (def shader (GL20/glCreateShader GL20/GL_VERTEX_SHADER))
  (def shader (GL20/glCreateShader GL20/GL_FRAGMENT_SHADER))


  (GL20/glShaderSource shader source)
  (GL20/glCompileShader shader)
  (when (zero? (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))
    (throw (Exception. (GL20/glGetShaderInfoLog shader 1024))))
  shader


  (def fragment-source (slurp (io/resource "cev/shaders/simple.frag")))

  (def vertex-shader (make-shader vertex-source GL20/GL_VERTEX_SHADER))

  (def fragment-shader (make-shader fragment-source GL20/GL_FRAGMENT_SHADER))
  (def program (make-program vertex-shader fragment-shader))
  program
  )
