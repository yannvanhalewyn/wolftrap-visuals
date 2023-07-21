(ns cev.dev.nrepl
  (:require
   [dev]
   [cev.main :as main]
   [clojure.string :as str]
   [nrepl.server :as nrepl]
   [cider.nrepl]))

(set! *print-namespace-maps* false)

(defn -main [& args]
  (let [port (Integer/parseInt (str/trim (slurp ".nrepl-port")))
        flag? (set args)]
    (nrepl/start-server :port port :handler cider.nrepl/cider-nrepl-handler)
    (println "nREPL started on port" port)

    (when (flag? "--watch-shaders")
      (dev/start-shader-refresher!))

    (when-not (flag? "--no-window")
      (apply main/-main args))))
