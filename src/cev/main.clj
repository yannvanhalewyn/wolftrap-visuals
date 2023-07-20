(ns cev.main
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.window :as window]
   [cev.midi :as midi])
  (:import
   [org.lwjgl.glfw GLFW]))

(defn- handle-midi! [msg]
  (db/dispatch! [::midi/event-received msg]))

(defn- run! [width height]
  (try
    (window/run! width height "Wolftrap Visuals")
    (catch Exception e
      (println "FATAL" e)
      (System/exit -1))
    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))

(defn -main [& _args]
  (midi/add-listener! handle-midi!)
  (run! 800 600))
