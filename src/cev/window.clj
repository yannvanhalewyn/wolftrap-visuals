(ns cev.window
  (:refer-clojure :exclude [run!])
  (:require
   [cev.db :as db]
   [cev.log :as log]
   [cev.particle :as particle]
   [cev.entities :as entities]
   [cev.renderer :as renderer]
   [cev.engine.renderer :as gl.renderer]
   [cev.engine.timer :as timer]
   [cev.engine.window :as window])
  (:import
   [clojure.lang PersistentQueue]
   [org.lwjgl.glfw GLFW]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Queue

(def ^:private queue (atom (PersistentQueue/EMPTY)))

(defn- popall! []
  (first (reset-vals! queue (PersistentQueue/EMPTY))))

(defn- enqueue! [messages]
  (when seq
    (swap! queue #(apply conj % messages))))

(db/reg-fx :gl/enqueue enqueue!)

(defn- exec-messages [messages]
  (loop [[[action :as message] & other-messages] messages
         events []]
    (if action
      (let [[success-event err-event] (renderer/handle-queue-message! message)]
        (if success-event
          (recur other-messages (conj events success-event))
          [events err-event]))
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
        ;; Also dispatch error so we can dissoc the failed entity
        ;; This makes me think interceptors might be worth it.
        (log/error :gl/handler-error error))
      (doseq [event events]
        (db/dispatch! event)))))

(defn populate! []
  ;; (db/dispatch! [::renderer/set-entities [entities/texture entities/texture2]])
  (db/dispatch! [::particle/init 200]))

(defn- key-callback [window key _scancode action _mods]
  (when (= action GLFW/GLFW_RELEASE)
    (condp = key
      GLFW/GLFW_KEY_Q
      (window/set-should-close! window true)

      GLFW/GLFW_KEY_R
      (populate!)

      nil)))

(defn- update! [timer throttle]
  (let [dt (timer/elapsed timer)]
    (when (timer/throttled? timer throttle)
      (log/info :fps (timer/fps timer)))
    (handle-queue!)

    (doseq [particle (first (db/subscribe [::particle/particles]))]
      (particle/update! particle dt))))

(defn- draw! [window timer]
  (let [resolution (window/get-resolution window)]
    (window/draw-frame!
     window

     ;; Entity renderer
     (doseq [[entity renderer] (db/subscribe [::renderer/all])]
       (when-not (contains? entity :cev.renderer/batch-id)
         (if renderer
           (gl.renderer/batch
            renderer
            (gl.renderer/bind-uniform-2f renderer "resolution" width height)
            (gl.renderer/bind-uniform-1f renderer "iterations" (db/subscribe [::midi/cc-value 72 [1.0 20.0]]))
            (gl.renderer/bind-uniform-1f renderer "complexity" (db/subscribe [::midi/cc-value 79 [0.0 1.0]]))
            (gl.renderer/bind-uniform-1f renderer "brightness" (db/subscribe [::midi/cc-value 91 [0.01 1.0]]))
            (gl.renderer/bind-uniform-1f renderer "time" (GLFW/glfwGetTime))
            (gl.renderer/draw1! renderer))
           (when (timer/throttled? timer)
             (log/error :gl/render-error "No renderer (entity renderer)")))))

     ;; Particles renderer
     (let [[particles [gl-blueprint renderer]] (db/subscribe [::particle/particles])]
       (when (timer/throttled? timer)
         #_(log/debug :rendering :particles (count particles)))
       (when (seq particles)
         (if renderer
           (gl.renderer/batch
            renderer
            (doseq [particle particles]
              (gl.renderer/transmit-uniforms!
               renderer gl-blueprint (merge {::window/resolution resolution}
                                            particle))
              (gl.renderer/draw1! renderer)))
           (when (timer/throttled? timer)
             (log/error :gl/render-error "No renderer (batch renderer)"))))))))

(defn make-interceptor [window]
  {::db/before
   (fn [cofx]
     (merge cofx {:gl/window window} {::window/resolution (window/get-resolution window)}))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Running

(defn run! [width height title]
  (populate!)
  (window/with-window [window {::window/width width
                               ::window/height height
                               ::window/title title
                               ::window/key-callback key-callback}]
    (db/reg-interceptor :gl/window (make-interceptor window))

    (let [fps-timer (timer/start :window/frames)
          throttle (timer/add-throttle! fps-timer 5)]
      (while (not (window/should-close? window))
        (update! fps-timer throttle)
        (draw! window fps-timer)
        (window/poll-events!)
        (timer/tick fps-timer)))
    (db/dispatch! [::particle/clear])))
