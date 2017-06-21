(ns directionalsurvey.facts)

(defn entryuser [v] [:global :entry/user ""])
(defn loginuser [v] [:global :loginuser v])
(defn user [name]
  (let [id (random-uuid)]
    [[id :user/name name]]))
