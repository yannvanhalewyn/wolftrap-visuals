(ns cev.main
  (:require
   [cev.window :as window]
   [nrepl.server :as nrepl]))

(defn -main [& args]
  (nrepl/start-server :port 7888)
  (window/init))
