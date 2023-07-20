(ns dev
  (:require
   [cev.db :as db]
   [cev.entities :as entities]))

(comment
  (db/dispatch! [::entities/set (entities/enabled-entities)])

  (db/dispatch! [::entities/set [entities/fractal-canvas]])
  (db/dispatch! [::entities/set [entities/rgb-triangle]])
  (db/dispatch! [::entities/set [entities/texture]])

  (db/handle-midi! {:control 72 :value 10})
  (db/handle-midi! {:control 79 :value 25})
  (db/handle-midi! {:control 91 :value 0})


  )
