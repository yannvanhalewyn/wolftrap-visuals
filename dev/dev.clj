(ns dev
  (:require [cev.db :as db]))

(comment
  (db/handle-midi! {:control 72 :value 10})
  (db/handle-midi! {:control 79 :value 25})
  (db/handle-midi! {:control 91 :value 0})


  )
