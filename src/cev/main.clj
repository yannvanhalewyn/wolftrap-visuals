(ns cev.main
  (:require
   [cev.db :as db]
   [cev.gl-context :as gl-context]
   [cev.midi :as midi]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl]))

(defn- handle-midi! [msg]
  (db/handle-midi! msg))

(defn -main [& _args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server :port 7888)
  (midi/add-listener! handle-midi!)
  (gl-context/run! 800 600)
  (io/delete-file ".nrepl-port"))
