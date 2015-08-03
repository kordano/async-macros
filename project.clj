(defproject async-macros "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-npm "0.4.0"]
            [lein-ring "0.9.6"]]
  :source-paths ["src/cljs" "src/cljc"]
  :node-dependencies [[source-map-support "0.2.8"]
                      [express "4.13.3"]]
  :clean-targets ^{:protect false}["resources/js"]
  :cljsbuild {:builds
              {:test
               {:source-paths ["test"]
                :compiler {:output-to "resources/js/compiled.js"
                           :optimizations :whitespace
                           :pretty-print true}}}
              :test-commands
              {"test" ["phantomjs"
                       "resources/test/test.js"
                       "resources/test/test.html"]}})
