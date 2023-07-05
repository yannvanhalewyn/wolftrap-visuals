(ns cev.main
  (:require
   [clojure.java.io :as io]
   [cev.gl-context :as gl-context]
   [nrepl.server :as nrepl]))

(defn -main [& args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server :port 7888)
  (gl-context/run! 800 600)
  (io/delete-file ".nrepl-port"))
