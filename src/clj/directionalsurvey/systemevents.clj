(ns directionalsurvey.systemevents
  (:require [taoensso.sente :as sente]
            [ring.util.response :refer [response resource-response]]
            [directionalsurvey.facts :refer [origtableconfig localtableconfig globaltableconfig]]
            [directionalsurvey.utils :as utils]
            [datomic.api :as d :refer [db q]]
            [directionalsurvey.db :as mydb]
            [clojure.data :as da :refer [diff]]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [directionalsurvey.db :as db]))

(let [;; Serializtion format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep

      chsk-server
      (sente/make-channel-socket-server!
        (get-sch-adapter) {:packer packer})

      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]
  (def ring-ws-post ajax-post-fn)
  (def ring-ws-handoff ajax-get-or-ws-handshake-fn)
  (def receive-channel ch-recv)
  (def channel-send! send-fn)
  (def connected-uids connected-uids))

(defn connected-uids-change-handler [_ _ old new]
  (when (not= old new)
    (let [oldsk (:any old)
          newsk (:any new)
          newlogin (nth (diff oldsk newsk) 1)
          logininf (db/getusers)]
      (println "Connected uids change: %s" new)
      (println "oldsk: " oldsk)
      (println "newsk: " newsk)
      (println "newlogin: " newlogin)
      (println (str "logininf: " logininf))
      (doseq [wsid newsk]
        (doseq [fact logininf]
          ;(println (str "fact: " fact))
          (channel-send! wsid [:db/insert {:data fact}]))))))

(add-watch connected-uids :connected-uids connected-uids-change-handler)

;; Messages handler
(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]} params]
    (println "Login request: %s" params)
    (println "Session: %s" (str session))
    (if true
      (do
        ;; Successful login!!!
        (mydb/insertauser user-id "defaultpassword")
        {:status 200 :session (assoc session :uid user-id)}))))

(defn init-handler [{:keys [wsid]}]
  ;; Need to send back information for client to init
  (println "Received message init from client!!!")
  (channel-send! wsid [:db/insert {:data (origtableconfig (utils/init-tableconfig))}])
  (channel-send! wsid [:db/insert {:data (localtableconfig (utils/init-tableconfig))}])
  (channel-send! wsid [:db/insert {:data (globaltableconfig (utils/init-tableconfig))}])
  ;; Send login information
  (let [logininf (db/getusers)
        listactions (db/getactions)]
    (doseq [fact logininf]
      ;(println (str "fact: " fact))
      (channel-send! wsid [:db/insert {:data fact}]))
    (doseq [action listactions]
      (channel-send! wsid [:db/insert {:data action}]))))

(defn settablevalue-handler [{:keys [user row col val]}]
  (let [listactions (mydb/insertanaction {:user user
                                          :row row
                                          :col col
                                          :val val})]
    (println (str "list actions: " listactions))
    (doseq [wsid (:any @connected-uids)]
      (println (str "wsid: " wsid))
      (doseq [fact listactions]
        (channel-send! wsid [:db/insert {:data fact}])))))

(defn- ws-msg-handler []
  (fn [{:keys [event] :as msg} _]
    (let [[id data :as ev] event]
      (case id
        :db/init (init-handler data)
        :user/set-table-value (settablevalue-handler data)
        (println "Unmatched event: " id " data: " data)))))

(defn ws-message-router []
  (sente/start-chsk-router-loop! (ws-msg-handler) receive-channel))





