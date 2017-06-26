(ns directionalsurvey.rules
  (:require-macros [precept.dsl :refer [<- entity entities]])
  (:require [precept.accumulators :as acc]
            [precept.spec.error :as err]
            [directionalsurvey.serverevents :as se]
            [precept.util :refer [insert! insert-unconditional! retract! guid] :as util]
            [precept.rules :refer-macros [define defsub session rule]]
            [cognitect.transit :as t]
            [directionalsurvey.facts :refer [entryuser loginuser user]]))


(rule setdatabyuser
      [[_ :loginuser ?username]]
      [[_ :setdata ?changeDatas]]
      =>
      ;(.log js/console "setdata: ")
      ;(.log js/console "user: " ?username)
      ;(.log js/console (str "changeDatas: " ?changeDatas))
      (se/set-action ?username ?changeDatas))

(rule loginsucessful
      [[_ :login/successful true]]
      [?user <- [_ :entry/user ?username]]
      =>
      (insert-unconditional! [:global :loginuser ?username])
      (retract! ?user))
      ;(.log js/console "login sucessful action!!!")
      ;(.log js/console "username: " ?username))

(rule loginaction
      [[_ :button/pressed :login]]
      [?user <- [_ :entry/user ?username]]
      =>
      (.log js/console "loginaction")
      ;(retract! ?user)
      (se/loginHandler ?user ?username))

(defsub :allusers
        [?eids <- (acc/by-fact-id :e) :from [:user/name]]
        [(<- ?allusers (entities ?eids))]
        =>
        {:rawallusers ?allusers})

(defsub :loginuser
        [[_ :loginuser ?loginuser]]
        =>
        {:loginuser ?loginuser})

(defsub :loginentry
        [[_ :entry/user ?user]]
        =>
        {:entry/user ?user})

(defsub :mylocaltable
        [[_ :localtableconfig ?localtableconfig]]
        =>
        {:localtableconfig (let [r (t/reader :json)]
                             (t/read r ?localtableconfig))})

(defsub :myglobaltable
        [[_ :globaltableconfig ?globaltableconfig]]
        =>
        {:globaltableconfig (let [r (t/reader :json)]
                              (t/read r ?globaltableconfig))})

(defsub :myglobaltransacts
        [?eids <- (acc/by-fact-id :e) :from [:action/value]]
        [(<- ?allactions (entities ?eids))]
        =>
        {:rawallactions ?allactions})

(session app-session
         'directionalsurvey.rules
         :db-schema directionalsurvey.schema/db-schema
         :client-schema directionalsurvey.schema/client-schema)