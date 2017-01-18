(defproject cljnode "0.4.0-SNAPSHOT"
  :jvm-opts ["-Ddev_env=dev"
             "-Dnode=cljnode@127.0.0.1"
             "-Dmbox=mboxname"
             "-Dcookie=nocookie"
             "-Depmd_port=15000"]
  :description "Clojure + LFE"
  :url "https://github.com/clojang/lfecljapp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths [ ]
  :dependencies [[clojang "0.4.0-SNAPSHOT"]
                 [clojusc/twig "0.3.0"
                 [org.clojure/clojure "1.8.0"]]
  :main cljnode.core
  :source-paths ["src/clj"]
  :target-path "target/"
  :profiles {
    :uberjar {:aot :all}})
