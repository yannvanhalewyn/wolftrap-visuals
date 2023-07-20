(ns cev.gl.context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.entities :as entities]
   [cev.gl.mesh :as mesh]
   [cev.gl.shader :as shader]
   [cev.gl.window :as window]
   [cev.midi :as midi])
  (:import
   [clojure.lang PersistentQueue]
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

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
            [events (ex-info "Failed to load entity" entity)]))

        :gl/destroy-entity
        (let [gl-entity (first params)]
          (mesh/destroy! gl-entity)
          (recur other-messages
                 (conj events [:gl/destroyed-gl-entity (:gl/id gl-entity)])))

        (throw (ex-info "Unknown action" action)))
      [events nil])))

(defn- exec-queue!
  "This queue is in place in order to be able to dispatch GL operations needed
  to be done (such as (re)compiling shaders) from another thread than the main
  thread, as that would crash the program. An fx `:gl/enqueue` can be used to
  enqueue oprations.

  If an error occurs the queue will stop processing further messages, dispatch
  the results of what was achieved so far and print the error."
  []
  (when-let [messages (seq (popall!))]
    (let [[events error] (exec-messages messages)]
      (when error
        (println error))
      (doseq [event events]
        (db/dispatch! event)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Running

(defn- draw! [window]
  (let [[width height] (window/get-size window)]
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))

    (doseq [[_entity gl-entity] (db/subscribe [::entities/all])]
      (when-let [{:keys [:gl/program]} gl-entity]
        (shader/uniform-2f program "resolution" width height)
        (shader/uniform-1f program "iterations" (db/subscribe [::midi/cc-value 72 [1.0 20.0]]))
        (shader/uniform-1f program "complexity" (db/subscribe [::midi/cc-value 79 [0.0 1.0]]))
        (shader/uniform-1f program "brightness" (db/subscribe [::midi/cc-value 91 [0.01 1.0]]))
        (shader/uniform-1f program "time" (GLFW/glfwGetTime))
        (mesh/draw! gl-entity)))

    (GLFW/glfwSwapBuffers window)
    (GLFW/glfwPollEvents)))

;; TODO make a macro with-window-context and a body to be ran. Maybe bind should
;; close? Or let the body call windowShoudlClose
;; Or have an :init and a :draw option, might be simpler.
(defn run!
  [window-opts]
  (try
    (let [window (window/init window-opts)]
      (GL11/glEnable GL11/GL_DEPTH_TEST)
      (GL11/glEnable GL11/GL_BLEND)
      (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)

      (while (not (GLFW/glfwWindowShouldClose window))
        (exec-queue!)
        (draw! window))

      (db/dispatch! [:clear-entities])

      (GLFW/glfwDestroyWindow window))

    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))
