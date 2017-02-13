(defmodule lfecljapp
  (behaviour gen_server)
  ;; API
  (export (start_link 0))
  ;; gen_server callbacks
  (export (init 1)
          (handle_call 3)
          (handle_cast 2)
          (handle_info 2)
          (terminate 2)
          (code_change 3))
  ;; Only for tests
  (export (stop 1)
          (ping 2)
          (ping 3)))

;;;===================================================================
;;; Configuration
;;;===================================================================

;;;===================================================================
;;; API
;;;===================================================================

(defrecord state
  remote-pid
  (waiters '())
  ext-port-ref)

(defun server-name () (MODULE))

(defun start ()
  (start_link))

(defun start_link ()
  (gen_server:start_link
     `#(local ,(server-name)) (MODULE) '() '()))

(defun init (_args)
  (let ((port (lein-node:start)))
    (gen_server:cast (self) 'ping)
    `#(ok ,(make-state ext-port-ref port))))

(defun stop (reason)
  (gen_server:cast (server-name) `#(stop-test ,reason)))

(defun ping (state this-pid)
  (erlang:send (match-state-exp-port-ref state)
               `#(ping ,this-pid)))

(defun ping (host node mbox)
  (ping (lfeclj-util:make-name node host) mbox))

(defun handle_call (_ _ state)
  (tuple 'reply 'ok state))

(defun handle_cast
  ((`#(stop-test ,reason) state)
    `#(stop ,reason ,state))
  (('ping state)
   (let ((node (lfeclj-cfg:get 'node))
         (mbox (lfeclj-cfg:get 'mbox))
         (host (case (lfeclj-cfg:get 'host)
                 ('undefined (element 2 (inet:gethostname)))
                 (other other))))
     (ping host node mbox)
     (erlang:send_after (lfeclj-cfg:get 'ping-interval)
                        (self)
                        'ping)
     `#(noreply ,state)))
  ((message state)
    (logjam:err "Unhandled case: '~p'" `(,message))
    `#(noreply ,state)))

(defun handle_info
  (('ping (= (match-state remote-pid 'undefined) state))
    (gen_server:cast (self) 'ping)
    `#(noreply ,state))
  (('ping state)
   `#(noreply ,state))
  ((`#(pong ,pid) (= (match-state remote-pid 'undefined waiters waiters)
                     state))
    (logjam:info "Connection to java node established, pid: ~p" `(,pid))
    (link pid)
    (lists:foreach
      (lambda (x)
        (gen_server:cast (self) `#(wait-for-login ,x)))
      waiters)
   `#(noreply ,(make-state remote-pid pid
                           waiters waiters
                           ext-port-ref (state-ext-port-ref))))
  ((`#(pong ,_) state)
    `#(noreply ,state))
  ((`#(,port #(exit_status ,status)) (= (match-state ext-port-ref ext-port)
                                        state)) (when (== port ext-port))
    (logjam:err "External java app exited with status: '~p'" `(,status))
    `#(stop #(error #(java-app-exit ,status)) ,state))
  ((`#(EXIT ,pid ,reason) (= (match-state remote-pid remote-pid)
                             state)) (when (== pid remote-pid))
    (logjam:err "External java mbox exited with reason: '~p'" `(,reason))
    `#(stop #(error #(java-mbox-exit ,reason)) ,state))
  ((info state)
    (logjam:err "Unhandled info: '~p'" `(,info))
    `#(noreply ,state)))

(defun terminate (reason state)
  'ok)

(defun code_change (old-version state extra)
  (tuple 'ok state))

;;;===================================================================
;;; Internal functions
;;;===================================================================

(defun start-clojure ()
  (let* ((node (lfeclj-cfg:get 'node))
         ((tuple 'ok host) (inet:gethostname))
         (node-name (lfeclj-util:make-name node host))
         (cmd (lfeclj-cfg:get 'cmd))
         (priv-dir (code:priv_dir 'lfecljapp))
         (log-file-name (++ (atom_to_list (node)) "_clj.log"))
         (full-cmd (++ "java -Dnode=\"" node-name
                       "\" -Dmbox=\"" (lfeclj-cfg:get 'mbox)
                       "\" -Dcookie=\"" (atom_to_list (erlang:get_cookie))
                       "\" -Depmd_port=" (lfeclj-cfg:get 'epmd-port)
                       " -Dlogfile=\"" priv-dir "/" log-file-name
                       "\" -classpath " priv-dir "/" cmd " ")))
    (logjam:info "Starting clojure app with cmd: ~p" `(,full-cmd))
    (open_port `#(spawn ,full-cmd) (list 'exit_status))))

