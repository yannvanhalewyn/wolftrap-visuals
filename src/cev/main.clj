(ns cev.main
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.gl.context :as gl.context]
   [cev.entities :as entities]
   [cev.gl.window :as window]
   [cev.midi :as midi])
  (:import
   [org.lwjgl.glfw GLFW]))

(defn- handle-midi! [msg]
  (db/handle-midi! msg))

(defn- key-callback [window key scancode action mods]
  ;; (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      (db/dispatch! [:set-entities (entities/enabled-entities)])

      nil)))

(defn- run! [width height]
  (db/dispatch! [:set-entities (entities/enabled-entities)])
  (gl.context/run!
   {::window/width width
    ::window/height height
    ::window/title "Wolftrap Visuals"
    ::window/key-callback key-callback}))

(defn -main [& _args]
  (midi/add-listener! handle-midi!)
  (run! 800 600))
