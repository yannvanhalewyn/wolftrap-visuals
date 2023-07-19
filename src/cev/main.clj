(ns cev.main
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.gl.context :as gl.context]
   [cev.gl.entity :as entity]
   [cev.entities :as entities]
   [cev.gl.window :as window]
   [cev.midi :as midi])
  (:import
   [org.lwjgl.glfw GLFW]))

(defn- handle-midi! [msg]
  (db/handle-midi! msg))

(defn- key-callback [window key scancode action mods]
  (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (GLFW/glfwSetWindowShouldClose window true)

      GLFW/GLFW_KEY_R
      ;; TODO would work better if we decouple the compiled shader data from the
      ;; entity data, this way we don't have to merge-with merge e.a
      (try
        (db/update-entities! (entities/enabled-entities))
        (doseq [entity (db/entities)]
          (when-let [new-entity (entity/re-compile! entity)]
            (db/add-entity! new-entity)))
        (catch Exception e
          (println e)))

      nil)))

(defn- run! [width height]
  (gl.context/run!
   {::window/width width
    ::window/height height
    ::window/title "Wolftrap Visuals"
    ::window/key-callback key-callback}
   (entities/enabled-entities)))

(defn -main [& _args]
  (midi/add-listener! handle-midi!)
  (run! 800 600))
