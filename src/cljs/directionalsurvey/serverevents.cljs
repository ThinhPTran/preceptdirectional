(ns directionalsurvey.serverevents
  (:require [goog.dom :as gdom]
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

(defn- event-handler [[id data] _]
  (case id
    :chsk/state (.log js/console "Channel state message received!!!")
    :chsk/recv (.log js/console "App message received!!!")
    :chsk/handshake (.log js/console "Handshake message received!!!")
    (.log js/console "Unmatched connection event with " (str id) " and data " (str data))))

(sente/start-chsk-router-loop! event-handler receive-channel)






