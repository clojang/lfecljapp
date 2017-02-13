(ns cljnode.core
  (:require [clojang.core :as clojang :refer [! receive self]]
            [clojang.node :as node]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log]
            [clojusc.twig :as logger])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Server   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn server
  [cmd-chan]
  (logger/set-level! '[clojang cljnode] :info)
  (log/info
    (str
      "Starting Clojure node with nodename = "
      (System/getProperty "node.sname")))
  (let [init-state 0]
    (loop [png-count init-state]
      (match (receive)
        [:register caller]
          (do
            (log/infof "Got :register request from %s ..." caller)
            (mbox/link (self) caller)
            (! caller :linked)
            (recur png-count))
        [:ping caller]
          (do
            (log/infof "Got :ping request from %s ..." caller)
            (! caller :pong)
            (recur (inc png-count)))
        [:get-ping-count caller]
          (do
            (log/infof "Got :get-ping-count request from %s ..."  caller)
            (! caller png-count)
            (recur png-count))
        [:stop caller]
          (do
            (log/warnf "Got :stop request from %s ..." caller)
            (! caller :stopping)
            :stopped)
        [_ caller]
          (do
            (log/error "Bad message received: unknown command")
            (! caller [:error :unknown-command])
            (recur png-count))
        [_]
          (do
            (log/error "Bad message received: improperly formatted")
            (recur png-count))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn register
  [server-data]
  (async/>!! (get-in server-data [:bridge :channel]) :register))

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
  (async/>!! (get-in server-data [:bridge :channel]) :stop))

(defn shutdown
  [server-data]
  (stop server-data)
  (log/info "Shutting down ...")
  (mbox/close (get-in server-data [:bridge :mbox]))
  (async/close! (get-in server-data [:bridge :channel]))
  (async/close! (:command server-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Wrappers & Utilities   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn otp-bridge
  "Creates the following in order to facilitate core.async communications with
  the Clojure OTP server:

    1. A dedicated mbox for the OTP bridge (what recieves messages from the
       OTP server)
    2. The pid for the dedicated mbox
    3. A core.async channel for sending messages to the OTP server."
  []
  (let [bridge-mbox (mbox/add :otpbrige)
        bridge-pid (mbox/get-pid bridge-mbox)
        bridge-chan (async/chan)]
    (async/go-loop []
      (when-let [value (async/<! bridge-chan)]
        (! [value bridge-pid]))
      (recur))
    {:mbox bridge-mbox
     :pid bridge-pid
     :channel bridge-chan}))

(defn managed-server
  "This function wraps the Clojang OTP server, providing channels/objects
  useful for developers:

    1. A server channel that will receive a notification when the OTP server
       stops
    2. A command channel for ...
    3. OTP/core.async bridge data."
  []
  (let [cmd-chan (async/chan)
        server-chan (async/thread (server cmd-chan))]
    (async/go
      (loop []
        (if-let [value (async/<! server-chan)]
          (log/infof "Server stopped with message '%s'" value)
          (recur)))
      (do
        (async/close! server-chan)
        (async/>!! cmd-chan :shutdown)))
    ;; XXX create a loop that listens for messages on command channel
    {:command cmd-chan
     :server server-chan
     :bridge (otp-bridge)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Main Function   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main
  [& args]
  (let [{:keys [command] :as server-data} (managed-server)]
    (if-let [value (async/<!! command)]
      (shutdown server-data))))
