(ns cev.dev.nrepl
  (:require
   [cev.main :as main]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl]
   [cider.nrepl]))

(set! *print-namespace-maps* false)

(defn -main [& args]
  (let [port 7888]
    (spit ".nrepl-port" port)
    (nrepl/start-server :port port :handler cider.nrepl/cider-nrepl-handler)
    (println "nREPL started on port" port)
    (when-not (= (first args) "--no-window")
      (apply main/-main args))
    (io/delete-file ".nrepl-port")))
