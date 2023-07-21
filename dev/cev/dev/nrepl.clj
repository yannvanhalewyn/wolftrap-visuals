(ns cev.dev.nrepl
  (:require
   [dev]
   [cev.main :as main]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl]
   [cider.nrepl]))

(set! *print-namespace-maps* false)

(defn -main [& args]
  (let [port 7888
        flag? (set args)]
    (spit ".nrepl-port" port)
    (nrepl/start-server :port port :handler cider.nrepl/cider-nrepl-handler)
    (println "nREPL started on port" port)
    (when-not (flag? "--no-window")
      (apply main/-main args))
    (when (flag? "--watch-shaders")
      (dev/start-shader-refresher!))
    (io/delete-file ".nrepl-port")))
