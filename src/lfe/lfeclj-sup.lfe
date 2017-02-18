(defmodule lfeclj-sup
  (behaviour supervisor)
  ;; API
  (export (start_link 0))
  ;; Supervisor callbacks
  (export (init 1)))

;;;===================================================================
;;; State & Configuration
;;;===================================================================

(defun sup-name () (MODULE))
(defun default-sup-flags
  #m(strategy one_for_one
     intensity 5
     period 10))

;;;===================================================================
;;; Supervisor Implementation
;;;===================================================================

(defun start_link ()
  (start_link (sup-name) '()))

(defun start_link (mod args)
  (start_link `#(local, (sup-name)) mod args))

(defun start_link (sup mod args)
  (supervisor:start_link sup mod args))

(defun stop ()
  (exit (whereis (sup-name)) 'shutdown))

;;;===================================================================
;;; Callback Implementation
;;;===================================================================

(defun init (_args)
  (let ((children `(,(make-child 'lfecljapp)
                    ,(make-child 'lein-node))))
    `#(ok #(,default-sup-flags children))))

;;;===================================================================
;;; Support Functions
;;;===================================================================

(defun make-child (mod)
  (make-child mod 'start_link))

(defun make-child (mod func)
  (make-child mod func '()))

(defun make-child (mod func args)
  `#m(id ,mod
      start #(,mod ,func ,args)
      restart permanent
      shutdown 5000
      type worker
      module (,mod)))
