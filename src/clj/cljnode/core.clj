(ns cljnode.core
  (:require [cljnode.api :as api]
            [cljnode.server :as server]
            [clojang.core :as clojang :refer [!]]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojusc.twig :as logger])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Wrappers & Utilities   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn otp-bridge
  "This function creates the following in order to facilitate core.async
  communications with the Clojure OTP server:

    1. A dedicated mbox for the OTP bridge (what receives messages from the
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

(defn start
  "This function is wrapper function for server/run that first sets the
  logging level."
  [ch]
  (logger/set-level! '[clojang cljnode] :info)
  (server/run ch))

(defn managed-server
  "This function wraps the Clojang OTP server, providing channels/objects
  useful for developers:

    1. A server channel that will receive a notification when the OTP server
       stops
    2. A command channel for ...
    3. OTP/core.async bridge data."
  []
  (let [cmd-chan (async/chan)
        server-chan (async/thread (start cmd-chan))
        server-data {:command cmd-chan
                     :server server-chan
                     :bridge (otp-bridge)}]
    (async/go
      (loop []
        (if-let [value (async/<! server-chan)]
          (log/infof "Server stopped with message '%s'" value)
          (recur)))
      (async/>! cmd-chan :shutdown))
    server-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Main Function   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main
  "Entry point for the Clojure portion of the lfecljapp."
  [& args]
  (let [{:keys [command] :as server-data} (managed-server)]
    (if-let [value (async/<!! command)]
      (case value
        :shutdown (api/shutdown server-data)))))
