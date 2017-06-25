(ns directionalsurvey.schema
  (:require [precept.util :refer [guid]]
            [precept.schema :refer [attribute]]
            [precept.state :as state]))

(defn gen-db-schema []
  [(attribute :action/value
              :db.type/string)])

(defn gen-client-schema []
  [(attribute :action/edit
              :db.type/string)])

(def db-schema (gen-db-schema))
(def client-schema (gen-client-schema))




