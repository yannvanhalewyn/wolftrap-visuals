(ns cev.db
  (:refer-clojure :exclude [read])
  (:require
    [cev.db :as db]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FX

(defonce fx (atom {}))

(defn- execute-effect! [[effect-key arg]]
  (if-let [f (get @fx effect-key)]
    (f arg)
    (println "ERROR: unknown effect" effect-key)))

(defn reg-fx [key f]
  (swap! fx assoc key f))

(defonce interceptors (atom {}))

(defn reg-interceptor [k interceptor]
  (swap! interceptors assoc k interceptor))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DB

(defonce db (atom {}))

(reg-fx :db #(reset! db %))

(reg-interceptor :db {::before #(assoc % :db @db)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event handlers

(defmulti handle-event (fn [_ [event-name]] event-name))

(defmethod handle-event :default
  [_ [event-name]]
  (println "Error: Unknown event" event-name))

(defn dispatch! [event]
  (println :db/event (first event))
  (let [coeffects (reduce (fn [cofx interceptor]
                            ((::before interceptor) cofx))
                          {} (vals @interceptors))
        effects (handle-event coeffects event)]
    (doseq [effect effects]
      (execute-effect! effect))))

(reg-fx
 :dispatch
 (fn [events]
   (doseq [event events]
     (db/dispatch! event))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subscriptions

(defmulti read (fn [_ [name]] name))

(defmethod read :default
  [_ [sub-name]]
  (println "Error: Unknown subscription" sub-name))

(defn subscribe [query]
  (read @db query))
