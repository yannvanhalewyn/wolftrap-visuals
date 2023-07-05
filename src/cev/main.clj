(ns cev.main
  (:require
   [clojure.java.io :as io]
   [cev.window :as window]
   [nrepl.server :as nrepl]))

(defn -main [& args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server :port 7888)
  (window/init)
  (io/delete-file ".nrepl-port"))
