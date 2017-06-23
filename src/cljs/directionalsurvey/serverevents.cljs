(ns directionalsurvey.serverevents
  (:require [goog.dom :as gdom]
            [cognitect.transit :as t]
            [clojure.string :as str]
            [precept.core :refer [subscribe then]]
            [directionalsurvey.facts :refer [loginuser entryuser origtableconfig localtableconfig globaltableconfig]]
            [precept.util :refer [insert! insert-unconditional! retract! guid] :as util]
            [taoensso.sente :as sente :refer (cb-success?)]))

; Sente setup
(let [chsk-type :auto
      ;; Serializtion format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep

      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
        "/chsk" ; Must match server Ring routing URL
        {:type   chsk-type
         :packer packer})]
  (def receive-channel ch-recv)
  (def send-channel! send-fn)
  (def chsk chsk)
  (def chsk-state state))

; Common functions used to communicate with server


; Login handler
(defn loginHandler [user username]
    (if (str/blank? username)
      (js/alert "Please enter a user-id first")
      (do
        (.log js/console "Logging in with user-id %s" username)

        ;;; Use any login procedure you'd like. Here we'll trigger an Ajax
        ;;; POST request that resets our server-side session. Then we ask
        ;;; our channel socket to reconnect, thereby picking up the new
        ;;; session.

        (sente/ajax-lite "/login"
                         {:method :post
                          :headers {:X-CSRF-Token (:csrf-token @chsk-state)}
                          :params  {:user-id (str username)}}

                         (fn [ajax-resp]
                           (.log js/console "Ajax login response: %s" ajax-resp)
                           (let [login-successful? true] ; Your logic here

                             (if-not login-successful?
                               (.log js/console "Login failed")
                               (do
                                 (.log js/console "Login successful")
                                 (sente/chsk-reconnect! chsk)
                                 (then [:transient :login/successful true])))))))))

(defn handle-insert [rawdata]
  (let [[eid att val] (:data rawdata)]
    (.log js/console (str "Server requested to insert this "))
    (.log js/console (str "eid: " eid))
    (.log js/console (str "att: " att))
    (.log js/console (str "val: " val))
    (then [eid att (let [w (t/writer :json)]
                     (t/write w (clj->js val)))])))

; handle application-specific events
(defn- app-message-received [[msgType data]]
  (case msgType
    :db/insert (handle-insert data)
    (.log js/console "Unmatched application event!!!")))

; handle websocket handshake events
(defn- handshake-message-received [[wsid csrf-token hsdata isfirst]]
  (.log js/console "Handshake message:")
  (.log js/console "wsid: " (str wsid))
  (.log js/console "csrf-token: " (str csrf-token))
  (.log js/console "hsdata: " (str hsdata))
  (.log js/console "isFirst: " (str isfirst))
  (send-channel! [:db/init {:wsid wsid}]))

(defn- event-handler [[id data] _]
  (case id
    :chsk/state (.log js/console "Channel state message received!!!")
    :chsk/recv (app-message-received data)
    :chsk/handshake (handshake-message-received data)
    (.log js/console "Unmatched connection event with " (str id) " and data " (str data))))

(sente/start-chsk-router-loop! event-handler receive-channel)






