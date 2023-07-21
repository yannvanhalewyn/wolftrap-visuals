(ns cev.main
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.window :as window]
   [cev.midi :as midi]
   [cev.log :as log])

  (:import
   [org.lwjgl.glfw GLFW]))

(defn- run! [width height]
  (try
    (window/run! width height "Wolftrap Visuals")
    (catch Exception e
      (log/error :error/fatal e)
      (System/exit -1))
    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))

(defn -main [& _args]
  (midi/add-listener! #(db/dispatch! [::midi/event-received %]))
  (run! 800 600))
