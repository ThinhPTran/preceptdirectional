(defproject directionalsurvey "0.1.0-SNAPSHOT"
  :main directionalsurvey.core
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.229"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [http-kit "2.2.0"]
                 [ring "1.5.0"]
                 [ring/ring-json "0.3.1"]
                 [cheshire "5.7.1"]
                 [compojure "1.6.0"]
                 [cljsjs/bootstrap "3.3.6-1"]
                 [cljsjs/bootstrap-slider "7.0.1-0"]
                 [cljsjs/highcharts "5.0.4-0"]
                 [com.taoensso/sente "1.11.0"]
                 [preceptweb "0.3.2"]
                 [reagent "0.6.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.4" :exclusions [org.clojure/clojure]]
            [lein-ring "0.8.10" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]]
    :plugins      [[lein-figwheel "0.5.10" :exclusions [org.clojure/clojure]]]}}


  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "directionalsurvey.core/reload"}
     :compiler     {:main                 directionalsurvey.core
                    :optimizations        :none
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/dev"
                    :asset-path           "js/compiled/dev"
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            directionalsurvey.core
                    :optimizations   :advanced
                    :output-to       "resources/public/js/compiled/app.js"
                    :output-dir      "resources/public/js/compiled/min"
                    :elide-asserts   true
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})


