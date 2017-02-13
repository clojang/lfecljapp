(ns cljnode.api
  (:require [clojang.core :as clojang :refer [receive]]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn register
  [server-data]
  (async/>!! (get-in server-data [:bridge :channel]) :register)
  :ok)

(defn ping
  [server-data]
  (async/>!! (get-in server-data [:bridge :channel]) :ping)
  (receive (get-in server-data [:bridge :mbox])))

(defn get-ping-count
  [server-data]
  (async/>!! (get-in server-data [:bridge :channel]) :get-ping-count)
  (receive (get-in server-data [:bridge :mbox])))

(defn stop
  [server-data]
  (async/>!! (get-in server-data [:bridge :channel]) :stop)
  :ok)

(defn shutdown
  [server-data]
  (stop server-data)
  (log/info "Shutting down ...")
  (mbox/close (get-in server-data [:bridge :mbox]))
  (async/close! (get-in server-data [:bridge :channel]))
  (async/close! (:command server-data))
  :ok)
