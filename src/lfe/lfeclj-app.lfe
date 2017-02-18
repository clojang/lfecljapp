(defmodule lfeclj-app
  (behaviour application)
  (export (start 0)
          (start 2)
          (stop 1)))

(defun start ()
  (logjam:start)
  (logjam:set-level 'lager_console_backend
                    (lcfg:get-in (lcfg-file:read-local) '(logging log-level)))
  (application:load 'lfecljapp)
  (application:start 'lfecljapp))

(defun start (_start-type _args)
  (let ((result (lfeclj-sup:start_link)))
    (case result
      (`#(ok ,pid) result)
      (_`#(error ,result)))))

(defun stop (_state)
  'ok)
