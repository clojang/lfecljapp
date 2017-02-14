(defproject cljnode "0.4.0-SNAPSHOT"
  :description "Clojure Node app in an LFE/OTP/Clojure System"
  :url "https://github.com/clojang/lfecljapp"
  :license {
    :name "Apache License, Version 2.0"
    :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clojang "0.4.0-SNAPSHOT"]
    [clojang/agent "0.4.0-SNAPSHOT"]
    [clojusc/twig "0.3.0"]
    [org.clojure/clojure "1.8.0"]
    [org.clojure/core.async "0.2.395"]
    [org.clojure/core.match "0.3.0-alpha4"]]
  :source-paths ["src/clj"]
  :profiles {
    :uberjar {
      :aot :all}
    :app {
      :main cljnode.core
      :jvm-opts ["-Dnode.sname=cljnode"]
      :java-agents [[clojang/agent "0.4.0-SNAPSHOT"]]}})
