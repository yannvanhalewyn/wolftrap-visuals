(ns cev.dev.watcher
  (:require
   [cev.log :as log]
   [watchtower.core :as watchtower]))

(defonce watcher (atom nil))

(defn setup-watcher! [dirs callback]
  (if @watcher
    (log/info :watcher/alread-running "Watcher already running.")
    (do
      (log/info :watcher/watcher "Started watching" dirs)
      (reset!
       watcher
       (watchtower/watcher dirs
        (watchtower/rate 100)
        ;; (watchtower/file-filter watchtower/ignore-dotfiles)
        ;; (watchtower/file-filter (watchtower/extensions :glsl))
        (watchtower/on-change
         (fn [files]
           (try
             (callback files)
             (catch Exception e
               (println "[Error]" :dev/file-watcher e))))))))))

(defn cancel-watcher! []
  (if-let [w @watcher]
    (do (future-cancel w)
        (reset! watcher nil))
    (log/info :watcher/not-running "Watcher was not running")))
