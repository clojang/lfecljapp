(ns cljnode.api
  (:require [clojang.core :as clojang :refer [receive]]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Support Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn send-only
  [server-data msg]
  (async/>!! (get-in server-data [:bridge :channel]) msg)
  :ok)

(defn send-and-receive
  [server-data msg]
  (send-only server-data msg)
  (receive (get-in server-data [:bridge :mbox])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn register
  [server-data]
  (send-only server-data :register))

(defn ping
  [server-data]
  (send-and-receive server-data :ping))

(defn get-ping-count
  [server-data]
  (send-and-receive server-data :get-ping-count))

(defn stop
  [server-data]
  (send-and-receive server-data :stop))

(defn shutdown
  [server-data]
  (stop server-data)
  (log/info "Shutting down ...")
  (mbox/close (get-in server-data [:bridge :mbox]))
  (async/close! (get-in server-data [:bridge :channel]))
  (async/close! (:command server-data))
  :ok)
