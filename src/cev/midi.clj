(ns cev.midi
  "https://github.com/clojure-interop/java-jdk/blob/master/javax.sound/src/javax/sound/midi/MidiSystem.clj
  https://stackoverflow.com/questions/51013510/receive-midi-input-with-javax-sound-midi
  "
  (:require
   [cev.db :as db])
  (:import
   [javax.sound.midi MidiSystem]
   [javax.sound.midi MidiMessage]
   [javax.sound.midi Receiver]
   [javax.sound.midi ShortMessage]
   [javax.sound.midi MidiUnavailableException]))

(defn- parse-message [message]
  {:status (.getStatus message)
   :control (.getData1 message)
   :value (.getData2 message)})

(defn- normalize
  ([value]
   (if value
     (float (or (/ value 127) 0))
     (float 0)))
  ([value [min max]]
   (-> (normalize value)
       (* (- max min))
       (+ min))))

(defn add-listener! [callback]
  (let [receiver (proxy [Receiver] []
                   (close [] nil)
                   (send [message timeStamp]
                     (callback (parse-message message))))]
    (doseq [device-info (MidiSystem/getMidiDeviceInfo)]
      (let [device (MidiSystem/getMidiDevice device-info)]
        ;; Does it need to do it for every transmitter? What are all the
        ;; transmitters? Minilab has 6
        ;; (doseq [transmitter (.getTransmitters device)]
        ;;   (println transmitter))
        (try
          (println "Adding MIDI listener to" (.. device getDeviceInfo getName))
          (let [transmitter (.getTransmitter device)]
            (.setReceiver transmitter receiver))
          (.open device)
          (catch MidiUnavailableException e
            (println "Error:" (str device-info) (.getMessage e))))))))

(defmethod db/run-event ::event-received
  [{:keys [db]} [_ msg]]
  (println "MIDI" msg)
  {:db (assoc-in db [::midi-cc (:control msg)] (:value msg))})

(defmethod db/read ::cc-value
  [db [_ cc range]]
  (normalize (get-in db [::midi-cc cc]) range))
