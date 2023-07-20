(ns cev.db
  (:refer-clojure :exclude [read]))

(defonce db (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FX

(defonce fx (atom {}))

(defn reg-fx [key f]
  (swap! fx assoc key f))

(reg-fx :db #(reset! db %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event handlers

(defmulti run-event (fn [_ [event-name]] event-name))

(defn dispatch! [event]
  (let [coeffects {:db @db}
        effects (run-event coeffects event)]
    (doseq [[effect-key arg] effects]
      (if-let [f (get @fx effect-key)]
        (f arg)
        (println "ERROR: unknown effect" effect-key)))))

(defmulti read (fn [_ [name]] name))

(defn subscribe [query]
  (read @db query))
