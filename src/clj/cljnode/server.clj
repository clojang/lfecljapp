(ns cljnode.server
  (:require [clojang.core :as clojang :refer [! receive self]]
            [clojang.mbox :as mbox]
            [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Server   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run
  [cmd-chan]
  (log/info "Starting Clojure node with nodename ="
            (System/getProperty "node.sname"))
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
