(ns dev
  (:require
   [sc.api]
   [cev.db :as db]
   [cev.midi :as midi]
   [cev.engine.math :as math]
   [cev.particle :as particle]
   [cev.renderer :as renderer]
   [cev.entities :as entities]
   [cev.dev.watcher :as watcher]
   [cev.util.ansi :as ansi]))

(set! *warn-on-reflection* true)
(set! *print-namespace-maps* true)

(defn db [] @db/db)

(defmethod db/handle-event ::refresh
  [{:keys [db]} _]
  {:dispatch [[::renderer/set-entities (vals (::renderer/entities db))]]})

(defn start-shader-watcher! []
  (watcher/setup-watcher!
   ["src/cev/shaders/"]
   ;; TODO refresh is not working
   (fn [_files] (db/dispatch! [::particle/init 1]))))

(defn stop-shader-watcher! []
  (watcher/cancel-watcher!))

(defn report-keys [coll keys]
  (doseq [key keys]
    (when (contains? coll key)
      (println (ansi/magenta key) (get coll key))))
  (println (ansi/gray "---")))

(defn inspect-db!
  ([]
   (inspect-db! @db/db))
  ([db]
   (println (ansi/cyan (str "\n#" :particles)))
   (doseq [particle (take 5 (::particle/particles db))]
     (report-keys (update particle :particle/position math/->clj)
                  [:particle/position :particle/size]))

   (when (> (count (::particle/particles db)) 5)
     (println "...and" (- (count (::particle/particles db)) 5) "more."))

   (println (ansi/cyan (str "\n#" :entities)))
   (doseq [entity (vals (::renderer/entities db))]
     (report-keys entity [:entity/name :entity/id ::id ::renderer/batch-id]))

   (println (ansi/cyan (str "\n# " :renderers)))
   (doseq [renderer (vals (::renderer/renderers db))]
     (report-keys renderer [::id :gl/vao :gl/program]))
   (println "")))

(db/reg-fx :dev/inspect-db #(when % (inspect-db! (db))))

(comment

  (start-shader-watcher)
  (stop-shader-watcher!)

  (inspect-db! @db/db)

  (db/subscribe [::renderer/all])

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
  (::particle/particles @db/db)


  )
