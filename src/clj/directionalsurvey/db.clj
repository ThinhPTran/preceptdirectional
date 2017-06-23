(ns directionalsurvey.db
  (:require [datomic.api :as d :refer [db q]]))

(def uri "datomic:mem://test")
(d/create-database uri)
(def db-connection (d/connect uri))

;(defn insertauser [name password]
;  (let [tmp @(d/transact db-connection
;                         [{:db/id #db/id[:db.part/user]
;                           :user/name name
;                           :user/password password}])
;        mydb (d/db db-connection)
;        rawdata (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
;                     :where [?e :user/name]]
;                   mydb)]
;    (println (str "users: " rawdata))))


(defn initdb []
  (let [schema (read-string (slurp "resources/directionalsurvey.edn"))]
    (println "set schema")
    (d/transact db-connection schema)))








