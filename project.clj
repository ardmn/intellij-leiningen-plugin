(defproject de.janthomae.leiningenplugin/leiningenplugin "1.0.0-SNAPSHOT"
   :description "Leiningen plugin for IntelliJ"
   :dependencies [[org.clojure/clojure "1.5.1"]
                  [leiningen-core "2.1.3"]]
   :source-paths ["src"]
   :java-source-paths ["src"]
   :profiles
   {:dev
    {:plugins [[lein-midje "3.0.1"]]
     :dependencies [[midje "1.5.1" :scope "test"]]}})
