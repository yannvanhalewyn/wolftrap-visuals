(ns cev.renderer
  (:require
   [cev.db :as db]
   [cev.engine.renderer :as gl.renderer]
   [cev.log :as log]
   [cev.util.ansi :as ansi]
   [medley.core :as m]))

(defn get-renderer [db renderer-id]
  (some
   (fn [[entity renderer]]
     (when (= (::batch-id entity) renderer-id)
       [entity renderer]))
   (db/read db [::all])))

(defn handle-queue-message!
  "Handling GL messages happen on the main render thread which has the GL
  Context bound. These calls are not permitted from outside of that thread,
  which is why there is some orchestration with queues and events involved in
  order to run this code."
  [[action & params]]
  (log/info :gl/action (ansi/bold (ansi/cyan action)))
  (case action
    :gl/load-renderer
    (let [entity (first params)]
      (if-let [renderer (gl.renderer/load! entity)]
        [[:gl/load-renderer--success (:entity/id entity) renderer] nil]
        [nil [:gl/load-renderer--failure
              (select-keys entity [:entity/id :entity/name])]]))

    :gl/destroy-renderer
    (let [renderer (first params)]
      (gl.renderer/destroy! renderer)
      [[:gl/destroy-renderer--success (::id renderer)] nil])

    [nil [:gl/unknown-action action]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Loading entities

(defmethod db/handle-event ::set-entities
  [{:keys [db]} [_ new-entities]]
  (let [renderers (vals (::renderers db))]
    {:db (assoc db ::entities (m/index-by :entity/id new-entities))
     :gl/enqueue (concat
                  (for [new-entity new-entities]
                    [:gl/load-renderer new-entity])
                  (for [renderer renderers]
                    [:gl/destroy-renderer renderer]))}))

(defmethod db/handle-event ::clear
  [{:keys [db]} _]
  {:db (dissoc db ::entities)
   :gl/enqueue (for [renderer (vals (::renderers db))]
                 [:gl/destroy-renderer renderer])})

(defmethod db/handle-event ::add
  [{:keys [db]} [_ entity]]
  {:db (assoc-in db [::entities (:entity/id entity)] entity)
   :gl/enqueue [[:gl/load-renderer entity]]})

(defmethod db/handle-event :gl/load-renderer--success
  [{:keys [db]} [_ entity-id renderer]]
  {:db (-> db
           (assoc-in [::renderers (::id renderer)] renderer)
           (assoc-in [::entities entity-id ::id] (::id renderer)))
   :dev/inspect-db false})

(defmethod db/handle-event :gl/destroy-renderer--success
  [{:keys [db]} [_ renderer-id]]
  {:db (m/dissoc-in db [::renderers renderer-id])
   :dev/inspect-db false})

(defmethod db/read ::all
  [{::keys [entities renderers]} _]
  (for [entity (vals entities)]
    [entity (get renderers (::id entity))]))
