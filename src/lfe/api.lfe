(defmodule api
  "This is a sample API for calling a Clojure server that has defined a
  handful of operations."
  (export all))

(defun send-msg (node-name msg)
  (! `#(default ,node-name) `#(,msg ,(self)))
  (receive
    (data data)))

(defun register (node-name)
  (send-msg node-name 'register))

(defun ping (node-name)
  (send-msg node-name 'ping))

(defun get-ping-count (node-name)
  (send-msg node-name 'get-ping-count))

(defun stop (node-name)
  (send-msg node-name 'stop))
