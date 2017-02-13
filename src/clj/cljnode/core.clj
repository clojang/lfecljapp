(ns cljnode.core
  (:require [clojang.core :as clojang :refer [! receive self]]
            [clojang.node :as node]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log]
            [clojusc.twig :as logger])
  (:gen-class))

(defn server
  []
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
            (log/info "Got :register request ...")
            (mbox/link (self) caller)
            (! caller :linked)
            (recur png-count))
        [:ping caller]
          (do
            (log/info "Got :ping request ...")
            (! caller :pong)
            (recur (inc png-count)))
        [:get-ping-count caller]
          (do
            (log/info "Got :get-ping-count request ...")
            (! caller png-count)
            (recur png-count))
        [:stop caller]
          (do
            (log/warn "Got :stop request ...")
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

;; XXX implement API for communicating with running server

(defn -main
    [& args]
    ;; XXX start server with core.async and create command channel
    ;; XXX Create a command channel OTP message box for receiving messages
    (server))
