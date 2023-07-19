(ns cev.dev.nrepl
  (:require
   [cev.main :as main]
   [clojure.java.io :as io]
   [nrepl.server :as nrepl]
   [cider.nrepl]))

(defn -main [& _args]
  (spit ".nrepl-port" 7888)
  (nrepl/start-server
   :port 7888
   :handler cider.nrepl/cider-nrepl-handler)
  (apply main/-main _args)
  (io/delete-file ".nrepl-port"))
