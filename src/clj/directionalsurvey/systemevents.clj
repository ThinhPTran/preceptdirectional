(ns directionalsurvey.systemevents
  (:require [taoensso.sente :as sente]
            [ring.util.response :refer [response resource-response]]
            [directionalsurvey.facts :refer [origtableconfig localtableconfig globaltableconfig]]
            [directionalsurvey.utils :as utils]
            [clojure.data :as da :refer [diff]]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

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
          newlogin (nth (diff oldsk newsk) 1)]
      (println "Connected uids change: %s" new)
      (println "oldsk: " oldsk)
      (println "newsk: " newsk)
      (println "newlogin: " newlogin))))

(add-watch connected-uids :connected-uids connected-uids-change-handler)

(defn init-handler [{:keys [wsid]}]
  ;; Need to send back information for client to init
  (println "Received message init from client!!!")
  (channel-send! wsid [:db/insert {:data (origtableconfig (utils/init-tableconfig))}])
  (channel-send! wsid [:db/insert {:data (localtableconfig (utils/init-tableconfig))}])
  (channel-send! wsid [:db/insert {:data (globaltableconfig (utils/init-tableconfig))}]))

(defn- ws-msg-handler []
  (fn [{:keys [event] :as msg} _]
    (let [[id data :as ev] event]
      (case id
        :db/init (init-handler data)
      ;  :db/action (action-processing data)
      ;  :db/changeAppState (changeState data)
        (println "Unmatched event: " id " data: " data)))))

(defn ws-message-router []
  (sente/start-chsk-router-loop! (ws-msg-handler) receive-channel))





