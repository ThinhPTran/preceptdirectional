(ns directionalsurvey.facts
  (:require [cognitect.transit :as t]))

(defn entryuser [v] [:global :entry/user ""])
(defn loginuser [v] [:global :loginuser v])

(defn origtableconfig [v] [:global :origtableconfig v])
(defn localtableconfig [v] [:global :localtableconfig v])
(defn globaltableconfig [v] [:global :globaltableconfig v])
