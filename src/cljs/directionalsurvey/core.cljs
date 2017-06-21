(ns directionalsurvey.core
  (:require [reagent.core :as reagent]
            [directionalsurvey.views :as views]
            [precept.core :refer [start! then]]
            [directionalsurvey.facts :refer [loginuser entryuser]]
            [directionalsurvey.rules :refer [app-session]]
            [directionalsurvey.schema :refer [db-schema]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn reload []
  (reagent/render [views/app]
                  (.getElementById js/document "app")))

(def facts (into (loginuser "Anonymous") (entryuser "")))

(defn ^:export main []
  (dev-setup)
  (start! {:session app-session :facts facts})
  (reload))
