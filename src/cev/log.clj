(ns cev.log
  (:require [cev.util.ansi :as ansi])
  (:import java.text.DateFormat java.util.Date))

(defn- format-date []
  (.format (DateFormat/getTimeInstance) (Date.)))

(defn- logger [type]
  (fn [& [component & args]]
    (apply println (ansi/gray (format-date)) type
           (ansi/bold (ansi/green component)) args)))

(def info (logger (ansi/gray "[info]")))
(def error (logger (ansi/red "[error]")))
(def debug (logger (ansi/yellow "[debug]")))
