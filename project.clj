(defproject cljnode "0.4.0-SNAPSHOT"
  :description "Clojure Node App in an LFE/OTP System"
  :url "https://github.com/clojang/lfecljapp"
  :license {
    :name "Apache License, Version 2.0"
    :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[clojang "0.4.0-SNAPSHOT"]
                 [clojang/agent "0.4.0-SNAPSHOT"]
                 [clojusc/twig "0.3.0"]
                 [org.clojure/clojure "1.8.0"]]
  :main cljnode.core
  :source-paths ["src/clj"]
  :jvm-opts ["-Dnode.sname=cljnode"
             "-Dnode.erlangcookie=.erlang.cookie"]
  :java-agents [[clojang/agent "0.4.0-SNAPSHOT"]]
  :profiles {
    :uberjar {:aot :all}})
