(defproject cljnode "0.4.0-SNAPSHOT"
  :jvm-opts ["-Ddev_env=dev"
             "-Dnode=cljnode@127.0.0.1"
             "-Dmbox=mboxname"
             "-Dcookie=nocookie"
             "-Depmd_port=15000"]
  :description "Clojure + LFE"
  :url "https://github.com/lfex/lfecljapp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths [ ]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                  [org.erlang.otp/jinterface "1.5.6"]]
  :main cljnode.core
  :source-paths ["src/clj"]
  :target-path "target/"
  :profiles {:uberjar {:aot :all}})
