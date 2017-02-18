(defmodule lfecljapp
  (behaviour gen_server)
  ;; API
  (export
    ;; gen_server implementation
    (start_link 0)
    (start 0)
    (stop 0)
    ;; callback implementation
    (init 1)
    (handle_call 3)
    (handle_cast 2)
    (handle_info 2)
    (terminate 2)
    (code_change 3)
    ;; server API
    (fing 0)
    (get-fing-count 0)))

;;;===================================================================
;;; State & Configuration
;;;===================================================================

(defrecord state
  remote-pid
  (waiters '())
  ext-port-ref)

(defun server-name () (MODULE))
(defun callback-module () (MODULE))
(defun initial-state () 0)
(defun genserver-opts () '())
(defun register-name () `#(local ,(server-name)))
(defun unknown-command () #(error "Unknown command."))

;;;===================================================================
;;; Gen Server Implementation
;;;===================================================================

(defun start ()
  (gen_server:start (register-name)
                    (callback-module)
                    (initial-state)
                    (genserver-opts)))

(defun stop ()
  (gen_server:call (server-name) 'stop))

;;;===================================================================
;;; Callback Implementation
;;;===================================================================

(defun init (initial-state)
  `#(ok ,initial-state))

(defun handle_call
  (('fing _caller state-data)
    `#(reply fong ,(+ 1 state-data)))
  (('fing-count _caller state-data)
    `#(reply ,state-data ,state-data))
  (('stop _caller state-data)
    `#(stop shutdown ok ,state-data))
  ((_message _caller state-data)
    `#(reply ,(unknown-command) ,state-data)))

(defun handle_info
  ((`#(EXIT ,_pid normal) state-data)
   `#(noreply ,state-data))
  ((`#(EXIT ,pid ,reason) state-data)
   (io:format "Process ~p exited! (Reason: ~p)~n" `(,pid ,reason))
   `#(noreply ,state-data))
  ((_msg state-data)
   `#(noreply ,state-data)))

(defun handle_info
  ((`#(EXIT ,_pid normal) state-data)
   `#(noreply ,state-data))
  ((`#(EXIT ,pid ,reason) state-data)
   (io:format "Process ~p exited! (Reason: ~p)~n" `(,pid ,reason))
   `#(noreply ,state-data))
  ((_msg state-data)
   `#(noreply ,state-data)))

(defun terminate (_reason _state-data)
  'ok)

(defun code_change (_old-version state _extra)
  `#(ok ,state))

;;;===================================================================
;;; Server API
;;;===================================================================

(defun fing ()
  (gen_server:cast (server-name) 'fing))

(defun get-fing-count ()
  (gen_server:cast (server-name) 'fing-count))
