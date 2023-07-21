(ns dev
  (:require
   [sc.api]
   [cev.db :as db]
   [cev.midi :as midi]
   [cev.particle :as particle]
   [cev.renderer :as renderer]
   [cev.entities :as entities]
   [cev.dev.watcher :as watcher]))

(defn start-shader-refresher! []
  (watcher/setup-watcher!
   ["src/cev/shaders/"]
   (fn [_files] (db/dispatch! [::renderer/refresh]))))

(defn stop-shader-refresher! []
  (watcher/cancel-watcher!))

(defn reload-shader-refresher! []
  (stop-shader-refresher!)
  (start-shader-refresher!))

(comment
  (start-shader-refresher!)
  (stop-shader-refresher!)



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

(db/dispatch! [::renderer/clear])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Particles

(db/dispatch! [::renderer/clear])

(db/dispatch! [::renderer/init 10])

(db/subscribe [::particle/particles])

(count (::renderer/entities @db/db))

(count (::renderer/entities @db/db))


  )
