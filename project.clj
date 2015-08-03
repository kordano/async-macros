(defproject async-macros "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :plugins [[lein-cljsbuild "1.0.6"]]
  :source-paths ["src"] 
  :clean-targets ^{:protect false}["target" "resources/test/compiled.js"]
  :cljsbuild {:builds
              {:test
               {:source-paths ["test"]
                :compiler {:output-to "resources/test/compiled.js"
                           :optimizations :whitespace
                           :pretty-print true}}}
              :test-commands
              {"test" ["phantomjs"
                       "resources/test/test.js"
                       "resources/test/test.html"]}})
