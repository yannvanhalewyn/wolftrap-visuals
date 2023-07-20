(ns dev
  (:require
   [cev.db :as db]
   [cev.midi :as midi]
   [cev.entities :as entities]))

(comment
  (db/dispatch! [::entities/set (entities/enabled-entities)])

  (db/dispatch! [::entities/set [entities/fractal-canvas]])
  (db/dispatch! [::entities/set [entities/rgb-triangle]])
  (db/dispatch! [::entities/set [entities/texture]])

  (db/dispatch! [::midi/event-received {:control 72 :value 10}])
  (db/dispatch! [::midi/event-received {:control 79 :value 25}])
  (db/dispatch! [::midi/event-received {:control 91 :value 0}])

  (db/subscribe [::midi/cc-value 72 [10 20]])


  )
