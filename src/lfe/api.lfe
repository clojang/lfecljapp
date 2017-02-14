(defmodule api
  "This is a sample API for calling a Clojure server that has defined a
  handful of operations."
  (export all))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Support Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defun send-only (node-name msg)
  (! `#(default ,node-name) `#(,msg ,(self))))

(defun send-and-receive (node-name msg)
  (send-only node-name msg)
  (receive
    (data data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defun register (node-name)
  (send-only node-name 'register))

(defun ping (node-name)
  (send-and-receive node-name 'ping))

(defun get-ping-count (node-name)
  (send-and-receive node-name 'get-ping-count))

(defun stop (node-name)
  (send-and-receive node-name 'stop))
