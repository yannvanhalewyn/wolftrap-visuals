(ns cev.window
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.entities :as entities]
   [cev.engine.mesh :as mesh]
   [cev.engine.shader :as shader]
   [cev.engine.window :as window]
   [cev.midi :as midi])
  (:import
   [clojure.lang PersistentQueue]
   [org.lwjgl.glfw GLFW]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Queue

(def ^:private queue (atom (PersistentQueue/EMPTY)))

(defn- popall! []
  (first (reset-vals! queue (PersistentQueue/EMPTY))))

(defn- enqueue! [messages]
  (swap! queue #(apply conj % messages)))

(db/reg-fx :gl/enqueue enqueue!)

(defn- exec-messages [messages]
  (loop [[[action & params] & other-messages] messages
         events []]

    (when action (println :gl/action action))

    (if action
      (case action

        :gl/compile-entity
        (let [entity (first params)]
          (if-let [gl-entity (mesh/load! entity)]
            (recur other-messages
                   (conj events [:gl/loaded-gl-entity (:entity/id entity) gl-entity]))
            [events (ex-info "Failed to load entity"
                             (select-keys entity [:entity/id :entity/name]))]))

        :gl/destroy-entity
        (let [gl-entity (first params)]
          (mesh/destroy! gl-entity)
          (recur other-messages
                 (conj events [:gl/destroyed-gl-entity (:gl/id gl-entity)])))

        (throw (ex-info "Unknown action" action)))
      [events nil])))

(defn- handle-queue!
  "This queue is in place in order to be able to dispatch GL operations needed
  to be done (such as (re)compiling shaders) asynchronously from another thread
  than the main thread, as that would crash the program since the GL context is
  thread bound. An fx `:gl/enqueue` can be used to enqueue oprations.

  If an error occurs the queue will stop processing further messages, dispatch
  the results of what was achieved so far and print the error."
  []
  (when-let [messages (seq (popall!))]
    (let [[events error] (exec-messages messages)]
      (when error
        (println error))
      (doseq [event events]
        (db/dispatch! event)))))

(defn- key-callback [window key scancode action mods]
  ;; (println "key-event" :key key :scancode scancode :action action :mods mods)

  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (window/set-should-close! window true)

      GLFW/GLFW_KEY_R
      (db/dispatch! [::entities/set (entities/enabled-entities)])

      nil)))

(defn- draw! [window]
  (let [[width height] (window/get-size window)]
    (window/draw-frame!
     window
     (doseq [[_entity gl-entity] (db/subscribe [::entities/all])]
       (when-let [{:keys [:gl/program]} gl-entity]
         (shader/uniform-2f program "resolution" width height)
         (shader/uniform-1f program "iterations" (db/subscribe [::midi/cc-value 72 [1.0 20.0]]))
         (shader/uniform-1f program "complexity" (db/subscribe [::midi/cc-value 79 [0.0 1.0]]))
         (shader/uniform-1f program "brightness" (db/subscribe [::midi/cc-value 91 [0.01 1.0]]))
         (shader/uniform-1f program "time" (GLFW/glfwGetTime))
         (mesh/draw! gl-entity))))))

(defn make-interceptor [window]
  {::db/inject-effect
   (fn [cofx]
     (let [[width height] (window/get-size window)]
       (assoc cofx
         :gl/window window
         ::window/width width
         ::window/height height)))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Running

(defn run! [width height title]
  (db/dispatch! [::entities/set (entities/enabled-entities)])
  (window/with-window [window {::window/width width
                               ::window/height height
                               ::window/title title
                               ::window/key-callback key-callback}]
    (db/reg-interceptor :gl/window (make-interceptor window))
    (while (not (window/should-close? window))
      (handle-queue!)
      (draw! window)
      (window/poll-events!))
    (db/dispatch! [::entities/clear])))
