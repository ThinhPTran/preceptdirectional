(ns directionalsurvey.facts
  (:require [cognitect.transit :as t]))

(defn entryuser [v] [:global :entry/user ""])
(defn loginuser [v] [:global :loginuser v])
(defn user [name]
  (let [id (random-uuid)]
    [[id :user/name name]]))
(defn origtableconfig [v] [:global :origtableconfig (let [w (t/writer :json)]
                                                      (t/write w v))])
(defn localtableconfig [v] [:global :localtableconfig (let [w (t/writer :json)]
                                                        (t/write w v))])
(defn globaltableconfig [v] [:global :globaltableconfig (let [w (t/writer :json)]
                                                          (t/write w v))])
