(ns directionalsurvey.db
  (:require [datomic.api :as d :refer [db q]]))

(def uri "datomic:mem://test")
(d/create-database uri)
(def db-connection (d/connect uri))

(defn now [] (new java.util.Date))

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

(defn insertanaction [{:keys [user row col val]}]
  ;; Query for username
  (let [rawexisted (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
                        :where [?e :user/name]]
                      (d/db db-connection))
        userentity (first (filterv #(= user (:user/name %)) rawexisted))]
    (println (str "userentity: " userentity))
    (let [tmp @(d/transact db-connection
                           [{:db/id #db/id[:db.part/user]
                             :action/user (:db/id userentity)
                             :action/row row
                             :action/col col
                             :action/val (double val)
                             :action/instant (now)}])
          rawdata (q '[:find [(pull ?e [:db/id :action/user :action/row :action/col :action/val :action/instant]) ...]
                       :where [?e :action/val]]
                     (d/db db-connection))
          outdata (mapv (fn [in] [(:db/id in)
                                  :action/value
                                  {:action/user (:action/user in)
                                   :action/row (:action/row in)
                                   :action/col (:action/col in)
                                   :action/val (:action/val in)
                                   :action/instant (:action/instant in)}]) rawdata)]
      outdata)))

(defn getusers []
  (let [rawdata (q '[:find [(pull ?e [:db/id :user/name :user/password]) ...]
                     :where [?e :user/name]]
                   (d/db db-connection))
        outdata (mapv (fn [in] [(:db/id in)
                                :user/name
                                (:user/name in)])
                      rawdata)]
    outdata))

(defn getactions []
  (let [rawdata (q '[:find [(pull ?e [:db/id :action/user :action/row :action/col :action/val :action/instant]) ...]
                     :where [?e :action/val]]
                   (d/db db-connection))
        rawdata (q '[:find [(pull ?e [:db/id :action/user :action/row :action/col :action/val :action/instant]) ...]
                     :where [?e :action/val]]
                   (d/db db-connection))
        outdata (mapv (fn [in] [(:db/id in)
                                :action/value
                                {:action/user (:action/user in)
                                 :action/row (:action/row in)
                                 :action/col (:action/col in)
                                 :action/val (:action/val in)
                                 :action/instant (:action/instant in)}]) rawdata)]
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
                                    :db/ident :action/col
                                    :db/valueType :db.type/long
                                    :db/cardinality :db.cardinality/one
                                    :db/doc "Column Index"
                                    :db.install/_attribute :db.part/db}
                                   {:db/id #db/id[:db.part/db]
                                    :db/ident :action/val
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









