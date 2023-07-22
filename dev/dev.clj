(ns dev
  (:require
   [sc.api]
   [cev.db :as db]
   [cev.midi :as midi]
   [cev.particle :as particle]
   [cev.renderer :as renderer]
   [cev.entities :as entities]
   [cev.dev.watcher :as watcher]
   [cev.util.ansi :as ansi]))

(defn db [] @db/db)

(defn start-shader-refresher! []
  (watcher/setup-watcher!
   ["src/cev/shaders/"]
   (fn [_files] (db/dispatch! [::renderer/refresh]))))

(defn stop-shader-refresher! []
  (watcher/cancel-watcher!))

(defn reload-shader-refresher! []
  (stop-shader-refresher!)
  (start-shader-refresher!))

(defn report-keys [coll keys]
  (doseq [key keys]
    (when (contains? coll key)
      (println (ansi/magenta key) (get coll key))))
  (println (ansi/gray "---")))

(defn inspect-db! [db]
  (println (ansi/cyan (str "\n#" :particles)))
  (doseq [entity (::particle/particles db)]
    (report-keys entity [:particle/position]))

  (println (ansi/cyan (str "\n#" :entities)))
  (doseq [entity (vals (::renderer/entities db))]
    (report-keys entity [:entity/name :entity/id :gl/id]))

  (println (ansi/cyan (str "\n# " :renderers)))
  (doseq [renderer (vals (::renderer/renderers db))]
    (report-keys renderer [:gl/id :gl/vao :gl/program]))
  (println ""))

(db/reg-fx :dev/inspect-db #(when % (inspect-db! (db))))

(comment

(start-shader-refresher!)
(stop-shader-refresher!)

(inspect-db! @db/db)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MIDI

(db/dispatch! [::midi/event-received {:control 72 :value 1}])
(db/dispatch! [::midi/event-received {:control 79 :value 50}])
(db/dispatch! [::midi/event-received {:control 91 :value 60}])

(db/subscribe [::midi/cc-value 72 [10 20]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entities

(db/dispatch! [::renderer/set-entities (entities/enabled-entities)])
(db/dispatch! [::renderer/set-entities [entities/fractal-canvas]])
(db/dispatch! [::renderer/set-entities [entities/rgb-triangle]])
(db/dispatch! [::renderer/set-entities [entities/texture]])
(db/dispatch! [::renderer/set-entities [entities/texture entities/texture2]])

(db/subscribe [::renderer/all])

(db/dispatch! [::renderer/clear])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Particles

(db/dispatch! [::particle/clear])

(db/dispatch! [::particle/init 10])

(db/subscribe [::particle/particles])

(count (::renderer/entities @db/db))

(count (::renderer/renderers @db/db))
(count (::particle/particles @db/db))


  )
