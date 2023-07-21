(ns cev.util.ansi)

(def supported?
  (let [term (get (System/getenv) "TERM")]
    (and term (not= term "dumb"))))

(def ^:private DEFAULT "\033[0m")

(defn- make [ansi s]
  (if supported?
    (str ansi s DEFAULT)
    s))

(defn bold [s]
  (make "\033[1m" s))

(defn red [s]
  (make "\033[0;31m" s))

(defn green [s]
  (make "\033[0;32m" s))

(defn yellow [s]
  (make "\033[0;33m" s))

(defn blue [s]
  (make "\033[0;34m" s))

(defn magenta [s]
  (make "\033[0;35m" s))

(defn cyan [s]
  (make "\033[0;36m" s))

(defn gray [s]
  (make "\u001b[38;5;240m" s))

(defn severity [s]
  (case s
    "DEBUG" (gray s)
    "INFO" (blue s)
    "NOTICE" (yellow s)
    "WARN" (yellow s)
    (red s)))
