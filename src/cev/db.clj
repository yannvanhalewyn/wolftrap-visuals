(ns cev.db
  (:refer-clojure :exclude [read])
  (:require
   [cev.util.ansi :as ansi]
   [cev.db :as db]
   [cev.log :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FX

(defonce fx (atom {}))

(defn- execute-effect! [[effect-key arg]]
  (if-let [f (get @fx effect-key)]
    (f arg)
    (log/error :db/unknown-effect "unknown effect" effect-key)))

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
  (log/error :db/unknown-event-handler "Unknown event" event-name))

(defn dispatch! [event]
  (log/info :db/event (ansi/bold (ansi/yellow (first event))))
  (when (nil? event)
    (throw (ex-info "Empty event" {:event event})))
  (let [coeffects (->> (keep ::before (vals @interceptors))
                       (reduce (fn [cofx before-fn] (before-fn cofx)) {}))
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
  (log/error :db/unknown-subscription "Unknown subscription" sub-name))

(defn subscribe [query]
  (read @db query))
