(ns cev.gl.context
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.gl.mesh :as mesh]
   [cev.gl.shader :as shader]
   [cev.gl.window :as window]
   [cev.midi :as midi])
  (:import
   [clojure.lang PersistentQueue]
   [org.lwjgl.glfw GLFW]
   [org.lwjgl.opengl GL11]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Compilation cache

(def compiled-entities (atom {}))

(defn- get-mesh [entity-id]
  (get @compiled-entities entity-id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Queue


(def ^:private queue (atom (PersistentQueue/EMPTY)))

(defn- popall! []
  (first (reset-vals! queue (PersistentQueue/EMPTY))))

(defn- enqueue! [messages]
  (swap! queue #(apply conj % messages)))

(db/reg-fx :gl/enqueue enqueue!)

(defn- exec-queue! []
  (doseq [[action & params] (popall!)]
    (println "GL:ACTION" action)
    (try
      (case action

        :gl/compile-entity
        (let [entity (first params)
              mesh (mesh/load! entity)]
          (swap! compiled-entities assoc (:entity/id entity) mesh))

        :gl/destroy-entity
        (let [entity-id (:entity/id (first params))
              mesh (get @compiled-entities entity-id)]
          (mesh/destroy! mesh)
          (swap! compiled-entities dissoc entity-id))

        (throw (ex-info "Unknown action" action)))
      (catch Exception e
        (println "ERROR!" e)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Running

(defn- draw! [window]
  (let [[width height] (window/get-size window)]
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT  GL11/GL_DEPTH_BUFFER_BIT))

    (doseq [entity (db/entities)]
      (when-let [{:keys [:gl/program] :as mesh} (get-mesh (:entity/id entity))]
        (shader/uniform-2f program "resolution" width height)
        (shader/uniform-1f program "iterations" (midi/normalize (db/midi-cc 72) 1.0 20.0))
        (shader/uniform-1f program "complexity" (midi/normalize (db/midi-cc 79)))
        (shader/uniform-1f program "brightness" (midi/normalize (db/midi-cc 91) 0.01 1.0))
        (shader/uniform-1f program "time" (GLFW/glfwGetTime))
        (mesh/draw! mesh)))

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

      (doseq [mesh (vals @compiled-entities)]
        (mesh/destroy! mesh))
      (GLFW/glfwDestroyWindow window))

    (finally
      (GLFW/glfwTerminate)
      (shutdown-agents)
      (System/exit 0))))
