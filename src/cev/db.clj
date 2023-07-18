(ns cev.db
  (:refer-clojure :exclude [get]))

(defonce db (atom {}))

(defn get [k]
  (clojure.core/get @db k))

(defn set-window! [window]
  (swap! db assoc :window window))

(defn set-mesh! [program mesh]
  (swap! db assoc :program program :mesh mesh))

(defn set-shaders! [vertex-shader fragment-shader]
  (swap! db assoc
         :vertex-shader vertex-shader
         :fragment-shader fragment-shader))

(defn current-shaders []
  (map @db [:vertex-shader :fragment-shader]))

(defn handle-midi! [msg]
  (println "MIDI" msg)
  (swap! db assoc-in [:midi-cc (:control msg)] (:value msg)))

(defn midi-cc [cc]
  (get-in @db [:midi-cc cc]))
