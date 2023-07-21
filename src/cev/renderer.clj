(ns cev.renderer
  (:require
   [cev.entities :as entities]
   [cev.db :as db]))

(defn get-renderer [db renderer-id]
  (some
   (fn [[entity renderer]]
     (when (= (:gl.renderer/id entity) renderer-id)
       renderer))
   (db/read db [::entities/all])))
