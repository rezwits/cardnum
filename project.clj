(defproject meccg "1.0"
  ;; the version string gets replaced by the git rev version plugin anyway
  :description "Browser implementation of MECCG card game."
  :url "https://github.com/rezwits/meccg"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/data.json "0.2.7"]
                 [cheshire "5.6.3"]
                 [org.omcljs/om "0.9.0"]
                 [sablono "0.3.4"]
                 [danhut/monger "3.1.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [differ "0.3.1"]
                 [com.taoensso/sente "1.11.0"]
                 [ring "1.6.2"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [aero "1.1.3"]
                 [buddy/buddy-sign "2.2.0"]
                 [buddy/buddy-auth "1.4.1"]
                 [crypto-password "0.2.0"]
                 [binaryage/devtools "0.9.7"]
                 [digest "1.4.6"]
                 [http-kit "2.6.0"]
                 [org.slf4j/slf4j-nop "1.7.12"]
                 [jwarwick/trello "0.3.3"]
                 [clj-time "0.14.2"]
                 [com.draines/postal "2.0.2"]
                 [throttler "1.0.0"]
                 [clj-http "3.7.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.15"]
            ;[com.gfredericks/lein-sha-version "0.1.1-p1"]
            [lein-ring "0.9.7"]
            [lein-exec "0.3.7"]]

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.15"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :plugins [[lein-figwheel "0.5.15"]]
                   :source-paths ["src/clj" "src/cljs" "src/dev" "src/cljc"]}}

  :aliases {"fetch" ["run" "-m" "tasks.fetch/fetch"]
            "add-art" ["run" "-m" "tasks.altart/add-art"]
            "delete-duplicate-users" ["run" "-m" "tasks.db/delete-duplicate-users"]
            "card-coverage" ["run" "-m" "tasks.cards/test-coverage"]}

  ;; Compilation.
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
    ;; aot only the namespaces needed for the main game in uberjar, notably ignoring the test and task namespaces.
  :aot [#"game\.*"
        #"web\.*"
        #"cardnum\.*"]
  :jar-name "meccg.jar"
  :uberjar-name "meccg-standalone.jar"
  :omit-source true
  :main web.core


  ;; Misc
  :test-paths ["test/clj"]

  :ring {:handler web.api/app}

  :cljsbuild {
    :builds [
      {:id "dev"
       :source-paths ["src/cljs" "src/dev" "src/cljc"]
       :figwheel true
       :compiler {:output-to "resources/public/cljs/app.js"
                  :output-dir "resources/public/cljs"
                  :optimizations :none
                  :source-map-timestamp true
                  :external-config {:devtools/config {:features-to-install :all}}}}
      {:id "prod"
       :source-paths ["src/cljs/meccg" "src/cljc"]
       :compiler {:output-to "resources/public/js/app.js"
                  :output-dir "out"
                  :optimizations :advanced
                  :pretty-print false
                  :externs ["src/cljs/externs/extras.js"
                            "src/cljs/externs/$.js"
                            "src/cljs/externs/howler.js"
                            "src/cljs/externs/io.js"
                            "src/cljs/externs/marked.js"
                            "src/cljs/externs/moment.js"
                            "src/cljs/externs/toastr.js"]}}]}

  :figwheel {:http-server-root "public"
             :server-port 3449
             :css-dirs ["resources/public/css"]}

  ;; Set timeout to 2 min to allow for full compilation after a clean.
  :repl-options {:timeout 120000
                 :init-ns web.core
                 :init (do (use 'web.lobby) (-main "dev"))})
