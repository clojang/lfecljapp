(ns cljnode.core
  (:require [cljnode.server :as server]
            [clojang.core :as clojang :refer [! receive self]]
            [clojang.node :as node]
            [clojang.mbox :as mbox]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log])
  (:gen-class))

(defn server
  ([]
    (server
      (System/getProperty "node.sname")
      (System/getProperty "node.erlangcookie")))
  ([node-name erlang-cookie]
    (log/info
      (format
        "Started with params:\n\tnodename: %s\n\tcookie: %s"
        node-name erlang-cookie))
    (let [init-state 0]
      (loop [png-count init-state]
        (match (receive)
          [:register caller]
            (do
              (mbox/link (self) caller)
              (! caller :linked)
              (recur png-count))
          [:ping caller]
            (do
              (! caller :pong)
              (recur (inc png-count)))
          [:get-ping-count caller]
            (do
              (! caller png-count)
              (recur png-count))
          [:stop caller]
            (do
              (! caller :stopping)
              :stopped)
          [_ caller]
            (do
              (! caller [:error :unknown-command])
              (recur png-count))
          [_]
            (do
              (log/warn "Bad message received: improperly formatted")
              (recur png-count)))))))

(defn -main
    [& args]
    (log/info "Starting Clojure node ...")
    (server))
