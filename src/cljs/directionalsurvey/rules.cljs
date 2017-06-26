(ns directionalsurvey.rules
  (:require-macros [precept.dsl :refer [<- entity entities]])
  (:require [precept.accumulators :as acc]
            [precept.spec.error :as err]
            [directionalsurvey.serverevents :as se]
            [precept.util :refer [insert! insert-unconditional! retract! guid] :as util]
            [precept.rules :refer-macros [define defsub session rule]]
            [cognitect.transit :as t]
            [directionalsurvey.utils :as dsutils]
            [directionalsurvey.facts :refer [entryuser loginuser user]]))

(rule globalactionschanged
      {:group :action}
      [[_ :globalactions ?globalactions]]
      [[_ :origtableconfig ?origtableconfig]]
      [[_ :currentpick ?currentpick]]
      [[_ :pickactions ?pickactions]]
      =>
      ;(.log js/console (str "globalactions: " ?globalactions))
      ;(.log js/console (str "origtableconfig: " ?origtableconfig))
      ;(.log js/console (str "currentpick: " ?currentpick))
      (let [origtableconfig (let [r (t/reader :json)]
                              (t/read r ?origtableconfig))
            globalactions ?globalactions
            pickactions ?pickactions]
        (dsutils/handle-global-table origtableconfig (seq pickactions))))

(rule localactionschanged
      {:group :action}
      [[_ :localactions ?localactions]]
      [[_ :origtableconfig ?origtableconfig]]
      [[_ :currentpick ?currentpick]]
      =>
      ;(.log js/console (str "localactions: " ?localactions))
      ;(.log js/console (str "origtableconfig: " ?origtableconfig))
      (let [origtableconfig (let [r (t/reader :json)]
                              (t/read r ?origtableconfig))
            localactions ?localactions]
        (dsutils/handle-local-table origtableconfig (seq localactions))))

(rule numberactionchanged
      {:group :action}
      [?Nactions <- (acc/count) :from [:action/value]]
      =>
      ;(.log js/console (str "Naction: " ?Nactions))
      (insert-unconditional! [:global :totalactions ?Nactions])
      (insert-unconditional! [:global :currentpick ?Nactions]))

(rule setdatabyuser
      {:group :action}
      [[_ :loginuser ?username]]
      [[_ :setdata ?changeDatas]]
      =>
      ;(.log js/console "setdata: ")
      ;(.log js/console "user: " ?username)
      ;(.log js/console (str "changeDatas: " ?changeDatas))
      (se/set-action ?username ?changeDatas))

(rule loginsucessful
      {:group :action}
      [[_ :login/successful true]]
      [?user <- [_ :entry/user ?username]]
      =>
      (insert-unconditional! [:global :loginuser ?username])
      (retract! ?user))
      ;(.log js/console "login sucessful action!!!")
      ;(.log js/console "username: " ?username))

(rule loginaction
      {:group :action}
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
        [[_ :currentpick ?currentpick]]
        =>
        {:rawallactions ?allactions
         :currentpick ?currentpick})

(defsub :myslider
        [[_ :globalactions ?globalactions]]
        [[_ :totalactions ?totalactions]]
        [[_ :currentpick ?currentpick]]
        =>
        {:globalactions ?globalactions
         :totalactions ?totalactions
         :currentpick ?currentpick})

(session app-session
         'directionalsurvey.rules
         :db-schema directionalsurvey.schema/db-schema
         :client-schema directionalsurvey.schema/client-schema)