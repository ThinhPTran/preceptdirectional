(ns directionalsurvey.db
  (:require [datomic.api :as d :refer [db q]]))

(def uri "datomic:mem://test")
(d/create-database uri)
(def db-connection (d/connect uri))

(defn insertauser [name password]
  (let [rawexisted (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
                        :where [?e :user/name]]
                     (d/db db-connection))
        existed (filterv #(= name (:user/name %)) rawexisted)]
    (if (> (count existed) 0)
      (println (str "existed: " existed))
      (let [tmp @(d/transact db-connection
                             [{:db/id #db/id[:db.part/user]
                               :user/name name
                               :user/password password}])
            rawdata (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
                         :where [?e :user/name]]
                       (d/db db-connection))]
        (println (str "users: " rawdata))))))

(defn getusers []
  (let [rawdata (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
                     :where [?e :user/name]]
                   (d/db db-connection))
        outdata (mapv (fn [in] [(:db/id in)
                                :user/name
                                (:user/name in)])
                      rawdata)]
    outdata))

(defn initdb []
  (println "set schema")
  (println "set user's schema")
  (time (d/transact db-connection [{:db/id #db/id[:db.part/db]
                                    :db/ident :user/name
                                    :db/valueType :db.type/string
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "A user's name"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id[:db.part/db]
                                    :db/ident :user/password
                                    :db/valueType :db.type/string
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "A user's password"
                                    :db.install/_attribute :db.part/db}]))
  (println "set action's schema")
  (time (d/transact db-connection [{:db/id #db/id[:db.part/db]
                                    :db/ident :action/user
                                    :db/valueType :db.type/ref
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "User does the action"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id[:db.part/db]
                                    :db/ident :action/row
                                    :db/valueType :db.type/long
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "Row Index"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id[:db.part/db]
                                    :db/ident :action/column
                                    :db/valueType :db.type/long
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "Column Index"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id[:db.part/db]
                                    :db/ident :action/newval
                                    :db/valueType :db.type/double
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "New value"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id [:db.part/db]
                                    :db/ident :action/instant
                                    :db/valueType :db.type/instant
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "Time stamp"
                                    :db.install/_attribute :db.part/db}])))









