(ns directionalsurvey.schema
  (:require [precept.util :refer [guid]]
            [precept.schema :refer [attribute]]
            [precept.state :as state]))

(defn gen-db-schema []
  [(attribute :action/act
              :db.type/string)
   (attribute :action/user
              :db.type/string)
   (attribute :action/row
              :db.type/long)
   (attribute :action/col
              :db.type/long)
   (attribute :action/val
              :db.type/double)
   (attribute :action/inst
              :db.type/instant)])

(defn gen-client-schema []
  [(attribute :action/edit
              :db.type/string)])

(def db-schema (gen-db-schema))
(def client-schema (gen-client-schema))




