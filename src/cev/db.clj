(ns cev.db
  (:require
   [medley.core :as m]))

(defonce db (atom {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Old mutations

(defn entities []
  (let [{:db/keys [entities gl-entities]} @db]
    (for [entity (vals entities)]
      [entity (get gl-entities (:gl/id entity))])))

(defn handle-midi! [msg]
  (println "MIDI" msg)
  (swap! db assoc-in [:midi-cc (:control msg)] (:value msg)))

(defn midi-cc [cc]
  (get-in @db [:midi-cc cc]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FX

(defonce fx (atom {}))

(defn reg-fx [key f]
  (swap! fx assoc key f))

(reg-fx :db #(reset! db %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event handlers

(defmulti run-event (fn [_ [event-name]] event-name))

(defmethod run-event :set-entities
  [{:keys [db]} [_ new-entities]]
  (let [old-gl-entities (vals (:db/gl-entities db))]
    {:db (assoc db :db/entities (m/index-by :entity/id new-entities))
     :gl/enqueue (concat
                  (for [new-entity new-entities]
                    [:gl/compile-entity new-entity])
                  (for [old-gl-entity old-gl-entities]
                    [:gl/destroy-entity old-gl-entity]))}))

(defmethod run-event :clear-entities
  [{:keys [db]} _]
  {:gl/enqueue (for [gl-entity (vals (:db/gl-entities db))]
                 [:gl/destroy-entity gl-entity])})

(defmethod run-event :gl/loaded-gl-entity
  [{:keys [db]} [_ entity-id gl-entity]]
  {:db (-> db
           (assoc-in [:db/gl-entities (:gl/id gl-entity)] gl-entity)
           (assoc-in [:db/entities entity-id :gl/id] (:gl/id gl-entity)))})

(defmethod run-event :gl/destroyed-gl-entity
  [{:keys [db]} [_ gl-entity-id]]
  {:db (m/dissoc-in db [:db/gl-entities gl-entity-id])})

(defn dispatch! [event]
  (let [coeffects {:db @db}
        effects (run-event coeffects event)]
    (doseq [[effect-key arg] effects]
      (if-let [f (get @fx effect-key)]
        (f arg)
        (println "ERROR: unknown effect" effect-key)))))
