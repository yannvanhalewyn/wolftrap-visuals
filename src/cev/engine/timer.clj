;; See https://legacy.lwjgl.org/javadoc/org/lwjgl/util/Timer.html for another
;; timer
(ns cev.engine.timer
  (:import org.lwjgl.glfw.GLFW))

(defn get-time
  "Returns the passed time since the GL context launched in seconds"
  []
  (GLFW/glfwGetTime))

(defn start
  ([id]
   (start id (get-time)))
  ([id curr-time]
   (atom {::id id
          ::prev-time (- curr-time 1)
          ::curr-time curr-time})))

(defn elapsed [timer]
  (let [{::keys [prev-time curr-time]} @timer]
    (- curr-time prev-time)))

(defn fps [timer]
  (/ 1 (elapsed timer)))

(defn- update-throttles
  [throttles curr-time]
  (if-let [outdated (seq
                     (for [[threshold last-hit] throttles
                           :when (>= (- curr-time last-hit) threshold)]
                       [threshold curr-time]))]
    (merge throttles (into {} outdated))
    throttles))

(defn throttle
  "Adds a throttle marker to the timer. This is useful if you want to use the
  timer to execute something once for every time period.

  See `throttled?` below."
  [timer threshold]
  (swap! timer assoc-in [::throttles threshold] (::curr-time @timer))
  threshold)

(defn throttled? [timer threshold]
  (let [{::keys [throttles curr-time]} @timer]
    (= (get throttles threshold) curr-time)))

(defn tick
  ([timer]
   (tick timer (get-time)))
  ([timer curr-time]
   (swap! timer
          (fn [state]
            (assoc state
              ::prev-time (::curr-time state)
              ::curr-time curr-time
              ::throttles (update-throttles (::throttles state) curr-time))))))

