(ns cev.db
  (:require [medley.core :as m])
  (:refer-clojure :exclude [get]))

(defonce db (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Old mutations

(defn entities []
  (:db/entities @db))

(defn handle-midi! [msg]
  (println "MIDI" msg)
  (swap! db assoc-in [:midi-cc (:control msg)] (:value msg)))

(defn midi-cc [cc]
  (get-in @db [:midi-cc cc]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FX

(def fx (atom {}))

(defn reg-fx [key f]
  (swap! fx assoc key f))

(reg-fx :db #(reset! db %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event handlers

(defmulti run-event (fn [_ [event-name]] event-name))

(defmethod run-event :set-entities
  [{:keys [db]} [_ new-entities]]
  (let [old-entities (:db/entities db)]
    {:db (assoc db :db/entities new-entities)
     :gl/enqueue (concat
                  (for [old-entity old-entities]
                    [:gl/destroy-entity old-entity])
                  (for [new-entity new-entities]
                    [:gl/compile-entity new-entity]))}))

(defn dispatch! [event]
  (let [coeffects {:db @db}
        effects (run-event coeffects event)]
    (doseq [[effect-key arg] effects]
      (if-let [f (clojure.core/get @fx effect-key)]
        (f arg)
        (println "ERROR: unknown effect" effect-key)))))
