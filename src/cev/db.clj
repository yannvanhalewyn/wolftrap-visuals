(ns cev.db
  (:refer-clojure :exclude [get]))

(defonce db (atom {}))

(defn get [k]
  (clojure.core/get @db k))

(defn set-window! [window]
  (swap! db assoc :window window))

(defn set-mesh! [program mesh]
  (swap! db assoc :program program :mesh mesh))

(defn add-entity! [entity]
  (swap! db update :db/entities conj entity))

(defn handle-midi! [msg]
  (println "MIDI" msg)
  (swap! db assoc-in [:midi-cc (:control msg)] (:value msg)))

(defn midi-cc [cc]
  (get-in @db [:midi-cc cc]))
