(ns cev.main
  (:require
   [clojure.java.io :as io]
   [cev.gl-context :as gl-context]
   [cev.midi :as midi]
   [nrepl.server :as nrepl]))

(defn- handle-midi! [msg]
  (println msg))

(defn -main [& _args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server :port 7888)
  (midi/add-listener! handle-midi!)
  (gl-context/run! 800 600)
  (io/delete-file ".nrepl-port"))
