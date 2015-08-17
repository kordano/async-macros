(defproject async-macros "0.1.0-SNAPSHOT"
  :description "Small debugging macros for cljs.core.async"
  :url "http://github.com/kordano/async-macros"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [com.cemerick/austin "0.1.6"]]
  :source-paths ["src" "src-cljs"] 
  :clean-targets ^{:protect false}["target" "out" "resources/test/compiled.js"]
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src-cljs"]
                :compiler {:output-to "out/main.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               :test
               {:source-paths ["test"]
                :compiler {:output-to "resources/test/compiled.js"
                           :optimizations :whitespace
                           :pretty-print true}}}
              :test-commands
              {"test" ["phantomjs"
                       "resources/test/test.js"
                       "resources/test/test.html"]}})
