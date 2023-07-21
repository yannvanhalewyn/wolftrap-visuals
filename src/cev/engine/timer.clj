;; See https://legacy.lwjgl.org/javadoc/org/lwjgl/util/Timer.html for another
;; timer
(ns cev.engine.timer
  (:import org.lwjgl.glfw.GLFW))

(defn get-time
  "Returns the passed time since the GL context launched in seconds"
  []
  (GLFW/glfwGetTime))

(defn start []
  (let [time (get-time)]
    (atom [time (- time 1)])))

(defn tick [timer]
  (swap! timer (fn [[old-current-time _]]
                 [(get-time) old-current-time])))

(defn elapsed [timer]
  (apply - @timer))

(defn fps [timer]
  (/ 1 (elapsed timer)))
