(ns cev.dev.watcher
  (:require
   [cev.log :as log]
   [watchtower.core :as watchtower]))

(defonce watcher (atom nil))

(defn cancel-watcher! []
  (if-let [w @watcher]
    (do (future-cancel w)
        (reset! watcher nil))
    (log/info :watcher "Watcher was not running")))

(defn setup-watcher! [dirs callback]
  (when @watcher
    (log/info :watcher "Watcher already running, stopping it first.")
    (cancel-watcher!))
  (log/info :watcher/watcher "Started watching" dirs)
  (reset!
   watcher
   (watchtower/watcher
    dirs
    (watchtower/rate 100)
    (watchtower/on-change
     (fn [files]
       (try
         (callback files)
         (catch Exception e
           (println "[Error]" :dev/file-watcher e))))))))
