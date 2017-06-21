(ns directionalsurvey.rules
  (:require-macros [precept.dsl :refer [<- entity entities]])
  (:require [precept.accumulators :as acc]
            [precept.spec.error :as err]
            [precept.util :refer [insert! insert-unconditional! retract! guid] :as util]
            [precept.rules :refer-macros [define defsub session rule]]
            [directionalsurvey.facts :refer [entryuser loginuser user]]))

(rule loguser
      [[_ :loginuser ?loginuser]]
      =>
      (insert-unconditional! (user ?loginuser)))

(rule loginaction
      [[_ :button/pressed :login]]
      [?user <- [_ :entry/user ?username]]
      =>
      (.log js/console "Rule works")
      (retract! ?user)
      (insert-unconditional! (loginuser ?username)))

(defsub :allusers
        [?eids <- (acc/by-fact-id :e) :from [:user/name]]
        [(<- ?allusers (entities ?eids))]
        =>
        {:allusers ?allusers})

(defsub :loginuser
        [[_ :loginuser ?loginuser]]
        =>
        {:loginuser ?loginuser})

(defsub :loginentry
        [[_ :entry/user ?user]]
        =>
        {:entry/user ?user})

(session app-session
         'directionalsurvey.rules
         :db-schema directionalsurvey.schema/db-schema
         :client-schema directionalsurvey.schema/client-schema)