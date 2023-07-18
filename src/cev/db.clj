(ns cev.db
  (:refer-clojure :exclude [get]))

(defonce db (atom {}))

(defn get [k]
  (clojure.core/get @db k))

(defn set-window! [window]
  (swap! db assoc :window window))

(defn add-entity! [entity]
  (swap! db assoc-in [:db/entities (:entity/id entity)] entity))

(defn entities []
  (vals (:db/entities @db)))

(defn handle-midi! [msg]
  (println "MIDI" msg)
  (swap! db assoc-in [:midi-cc (:control msg)] (:value msg)))

(defn midi-cc [cc]
  (get-in @db [:midi-cc cc]))
