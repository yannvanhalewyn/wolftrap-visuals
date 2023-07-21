(ns cev.particle
  (:require
   [cev.db :as db]
   [cev.engine.math :as math]
   [cev.engine.window :as window]))

(defn- make-particles [num-particles width height]
  (let [speed 20
        max-age 20]
    (repeatedly
     num-particles
     (fn []
       {:particle/position [(math/random (- width) width)
                            (math/random (- height) height)]
        :particle/velocity [(math/random (- speed) speed)
                            (math/random (- speed) speed)]
        :particle/age 0
        :particle/max-age max-age}))))

(defmethod db/handle-event ::init
  [{:keys [db ::window/width ::window/height]} [_ num-particles]]
  {:db (assoc db ::particles (make-particles num-particles width height))})
