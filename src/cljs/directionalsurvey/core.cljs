(ns directionalsurvey.core
  (:require [reagent.core :as reagent]
            [directionalsurvey.views :as views]
            [precept.core :refer [start! then]]
            [directionalsurvey.serverevents :as se]
            [directionalsurvey.utils :as utils]
            [directionalsurvey.facts :refer [loginuser entryuser origtableconfig localtableconfig globaltableconfig]]
            [directionalsurvey.utils :refer [init-tableconfig]]
            [directionalsurvey.rules :refer [app-session]]
            [directionalsurvey.schema :refer [db-schema]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(def facts (into []
                 [(entryuser "")]))

(defn startsession []
  (start! {:session app-session :facts facts}))

(defn reload []
  (reagent/render [views/app]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (startsession)
  (reload))
