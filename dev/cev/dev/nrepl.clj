(ns cev.dev.nrepl
  (:require
   [cev.main :as main]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl]
   [cider.nrepl]))

(set! *print-namespace-maps* false)

(defn -main [& _args]
  (let [port 7888]
    (spit ".nrepl-port" port)
    (nrepl/start-server :port port :handler cider.nrepl/cider-nrepl-handler)
    (println "nREPL started on port" port)
    (apply main/-main _args)
    (io/delete-file ".nrepl-port")))
