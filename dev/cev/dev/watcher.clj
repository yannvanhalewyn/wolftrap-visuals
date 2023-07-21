(ns cev.dev.watcher
  (:require [watchtower.core :as watchtower]))

(defonce watcher (atom nil))

(defn setup-watcher! [dirs callback]
  (if @watcher
    (println "Watcher already running.")
    (do
      (println "[Info]" "Started watching" dirs)
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
  (future-cancel @watcher)
  (reset! watcher nil))
